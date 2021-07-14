package com.future94.swallow.data.client.http;

import com.future94.swallow.common.dto.ConfigData;
import com.future94.swallow.common.exception.SwallowException;
import com.future94.swallow.common.thread.SwallowThreadFactory;
import com.future94.swallow.common.thread.ThreadUtils;
import com.future94.swallow.data.client.http.config.HttpConfig;
import com.future94.swallow.data.client.http.refresh.DataRefresh;
import com.future94.swallow.data.client.http.refresh.MetaDataRefresh;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.future94.swallow.data.common.api.SyncMetaDataService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author weilai
 */
@Slf4j
public class HttpSyncMetaDataService implements SyncMetaDataService, AutoCloseable {

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    private static final Gson GSON = new Gson();

    private final List<String> serverList;

    private final DataRefresh factory;

    private final HttpConfig httpConfig;

    /**
     * only use for http long polling.
     */
    private RestTemplate httpClient;

    private ExecutorService executor;

    public HttpSyncMetaDataService(final HttpConfig httpConfig, final List<MetaDataSubscriber> metaDataSubscribers) {
        this.httpConfig = httpConfig;
        this.serverList = Arrays.asList(httpConfig.getServerAddr().split(","));
        this.httpClient = createOkHttpRestTemplate();
        this.factory = new MetaDataRefresh(metaDataSubscribers);
        this.start();
    }

    private RestTemplate createOkHttpRestTemplate() {
        OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory();
        factory.setConnectTimeout(httpConfig.getConnectionTimeout());
        factory.setReadTimeout(httpConfig.getReadTimeout());
        return new RestTemplate(factory);
    }

    private void start() {
        if (RUNNING.compareAndSet(false, true)) {
            this.fetchGroupConfig();
            int threadSize = serverList.size();
            this.executor = new ThreadPoolExecutor(threadSize, threadSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), SwallowThreadFactory.create("swallow-http-long-polling", true));
            this.serverList.forEach(server -> this.executor.execute(new HttpLongPollingTask(server)));
        } else {
            log.info("http long polling was started, executor=[{}]", executor);
        }
    }

    @Override
    public void close() throws Exception {
        if (RUNNING.compareAndSet(true, false) && executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    private void fetchGroupConfig() throws SwallowException {
        for (int index = 0; index < this.serverList.size(); index++) {
            String server = serverList.get(index);
            try {
                this.doFetchGroupConfig(server);
                break;
            } catch (SwallowException e) {
                // no available server, throw exception.
                if (index >= serverList.size() - 1) {
                    throw e;
                }
                log.warn("fetch config fail, try another one: {}", serverList.get(index + 1));
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void doLongPolling(final String server) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>(8);
        ConfigData<?> cacheConfig = factory.cacheConfigData();
        params.add("md5", cacheConfig.getMd5());
        params.add("lastModifyTime", cacheConfig.getLastModifyTime());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity httpEntity = new HttpEntity(params, headers);
        String listenerUrl = server + "/sync/listener";
        log.debug("request listener configs: [{}]", listenerUrl);
        String data;
        try {
            String json = this.httpClient.postForEntity(listenerUrl, httpEntity, String.class).getBody();
            log.debug("listener result: [{}]", json);
            data = GSON.fromJson(json, JsonObject.class).get("data").getAsString();
        } catch (RestClientException e) {
            String message = String.format("listener configs fail, server:[%s], %s", server, e.getMessage());
            throw new SwallowException(message, e);
        }
        if ("METADATA".equals(data)) {
            // fetch group configuration async.
            log.info("Group config changed: {}", data);
            this.doFetchGroupConfig(server);
        }
    }

    private void doFetchGroupConfig(final String server) {
        String url = server + "/sync/fetch";
        log.info("request configs: [{}]", url);
        String json = null;
        try {
            json = this.httpClient.getForObject(url, String.class);
        } catch (RestClientException e) {
            String message = String.format("fetch config fail from server[%s], %s", url, e.getMessage());
            log.warn(message);
            throw new SwallowException(message, e);
        }
        boolean updated = this.updateCacheWithJson(json);
        if (updated) {
            log.info("get latest configs: [{}]", json);
            return;
        }
        // not updated. it is likely that the current config server has not been updated yet. wait a moment.
        log.info("The config of the server[{}] has not been updated or is out of date. Wait for 30s to listen for changes again.", server);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean updateCacheWithJson(final String json) {
        JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);
        JsonObject data = jsonObject.getAsJsonObject("data");
        // if the config cache will be updated?
        return factory.refresh(data);
    }

    class HttpLongPollingTask implements Runnable {

        private final String serverAddr;

        private final int maxRetry;

        private final int failDelayTime;

        private final int retryDelayTime;

        public HttpLongPollingTask(String serverAddr) {
            this.serverAddr = serverAddr;
            this.maxRetry = httpConfig.getMaxRetry();
            this.failDelayTime = httpConfig.getFailDelayTime();
            this.retryDelayTime = httpConfig.getRetryDelayTime();
        }

        @Override
        public void run() {
            while (RUNNING.get()) {
                for (int time = 1; time <= maxRetry; time++) {
                    try {
                        doLongPolling(serverAddr);
                    } catch (Exception e) {
                        // print warn log.
                        if (time < maxRetry) {
                            log.warn("Long polling failed, tried {} times, {} times left, will be suspended for a while! {}",
                                    time, maxRetry - time, e.getMessage());
                            ThreadUtils.sleep(TimeUnit.MILLISECONDS, retryDelayTime);
                            continue;
                        }
                        // print error, then suspended for a while.
                        log.error("Long polling failed, try again after {} minutes!", failDelayTime, e);
                        ThreadUtils.sleep(TimeUnit.MINUTES, failDelayTime);
                    }
                }
            }
            log.warn("Stop http long polling.");
        }

    }
}
