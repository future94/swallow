package com.future94.swallow.data.client.bootstrap.listener;

import com.future94.swallow.common.dto.ConfigData;
import com.future94.swallow.common.dto.SwallowCommonResponse;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.enums.DataEventTypeEnum;
import com.future94.swallow.common.exception.SwallowException;
import com.future94.swallow.common.thread.SwallowThreadFactory;
import com.future94.swallow.common.utils.GsonUtils;
import com.future94.swallow.common.utils.Md5Utils;
import com.future94.swallow.data.client.bootstrap.config.HttpSyncProperties;
import com.future94.swallow.data.client.bootstrap.entity.MetaData;
import com.future94.swallow.data.client.bootstrap.service.MetaDataService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author weilai
 */
@Slf4j
public class HttpLongPollingDataChangedListener implements DataChangedListener, InitializingBean {

    private static ConfigData<MetaDataRegisterDto> cache;

    private final ScheduledExecutorService scheduler;

    private final BlockingQueue<LongPollingClient> clients;

    private final HttpSyncProperties httpSyncProperties;

    private static final ReentrantLock LOCK = new ReentrantLock();

    @Resource
    private MetaDataService metaDataService;

    public HttpLongPollingDataChangedListener(final HttpSyncProperties httpSyncProperties) {
        this.clients = new ArrayBlockingQueue<>(1024);
        this.scheduler = new ScheduledThreadPoolExecutor(1,
                SwallowThreadFactory.create("swallow-long-polling", true));
        this.httpSyncProperties = httpSyncProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        updateMetaDataCache();
        long syncInterval = httpSyncProperties.getRefreshInterval().toMillis();
        // Periodically check the data for changes and update the cache
        scheduler.scheduleWithFixedDelay(() -> {
            log.info("http sync strategy refresh config start.");
            try {
                this.updateMetaDataCache();
                log.info("http sync strategy refresh config success.");
            } catch (Exception e) {
                log.error("http sync strategy refresh config error!", e);
            }
        }, syncInterval, syncInterval, TimeUnit.MILLISECONDS);
        log.info("http sync strategy refresh interval: {}ms", syncInterval);
    }

    @Override
    public void onMetaDataChanged(List<MetaData> changed, DataEventTypeEnum eventType) {
        if (CollectionUtils.isEmpty(changed)) {
            return;
        }
        this.updateMetaDataCache();
        this.afterMetaDataChanged();
    }

    public void doLongPolling(final HttpServletRequest request, final HttpServletResponse response) {
        // compare group md5

        String clientIp = getRemoteIp(request);
        // response immediately.
        if (isChangedMetaData(request)) {
            this.generateResponse(response, "METADATA");
            log.info("send response with the changed metadata, ip={}", clientIp);
            return;
        }
        // listen for configuration changed.
        final AsyncContext asyncContext = request.startAsync();
        // AsyncContext.setTimeout() does not timeout properly, so you have to control it yourself
        asyncContext.setTimeout(0L);
        // block client's thread.
        // TODO
        scheduler.execute(new LongPollingClient(asyncContext, clientIp, TimeUnit.SECONDS.toMillis(60)));
    }

    /**
     * get real client ip.
     *
     * @param request the request
     * @return the remote ip
     */
    private static String getRemoteIp(final HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (!org.apache.commons.lang3.StringUtils.isBlank(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String header = request.getHeader("X-Real-IP");
        return !StringUtils.hasText(header) ? request.getRemoteAddr() : header;
    }

    private void updateMetaDataCache() {
        this.updateCache(metaDataService.findAll());
    }

    private void updateCache(final List<MetaDataRegisterDto> data) {
        String json = GsonUtils.getInstance().toJson(data);
        ConfigData<MetaDataRegisterDto> newVal = new ConfigData<>(Md5Utils.md5(json), System.currentTimeMillis(), data);
        log.info("update metadata config cache, old: {}, updated: {}", cache, newVal);
        cache = newVal;
    }

    private void afterMetaDataChanged() {
        scheduler.execute(new DataChangeTask());
    }

    private void generateResponse(final HttpServletResponse response, String data) {
        try {
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Cache-Control", "no-cache,no-store");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(GsonUtils.getInstance().toJson(SwallowCommonResponse.success("success", data)));
        } catch (IOException ex) {
            log.error("Sending response failed.", ex);
        }
    }

    private boolean isChangedMetaData(final HttpServletRequest request) {
        String clientMd5 = request.getParameter("md5");
        String lastModifyTime = request.getParameter("lastModifyTime");
        if (!StringUtils.hasText(clientMd5) || !StringUtils.hasText(lastModifyTime)) {
            throw new SwallowException("group param invalid, md5:" + clientMd5 + ", lastModifyTime:" + lastModifyTime);
        }
        long clientModifyTime = NumberUtils.toLong(lastModifyTime);
        // do check.
        return this.checkCacheDelayAndUpdate(cache, clientMd5, clientModifyTime);
    }

    /**
     * check whether the client needs to update the cache.
     * @param serverCache the admin local cache
     * @param clientMd5 the client md5 value
     * @param clientModifyTime the client last modify time
     * @return true: the client needs to be updated, false: not need.
     */
    private boolean checkCacheDelayAndUpdate(final ConfigData<MetaDataRegisterDto> serverCache, final String clientMd5, final long clientModifyTime) {
        if (clientMd5.equals(serverCache.getMd5())) {
            return false;
        }
        // if the md5 value is different, it is necessary to compare lastModifyTime.
        long lastModifyTime = serverCache.getLastModifyTime();
        if (lastModifyTime >= clientModifyTime) {
            // the client's config is out of date.
            return true;
        }
        // the lastModifyTime before client, then the local cache needs to be updated.
        // Considering the concurrency problem, admin must lock,
        // otherwise it may cause the request from shenyu-web to update the cache concurrently, causing excessive db pressure
        boolean locked = false;
        try {
            locked = LOCK.tryLock(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
        if (locked) {
            try {
                ConfigData<MetaDataRegisterDto> latest = cache;
                if (latest != serverCache) {
                    // the cache of admin was updated. if the md5 value is the same, there's no need to update.
                    return !clientMd5.equals(latest.getMd5());
                }
                // load cache from db.
                this.updateMetaDataCache();
                latest = cache;
                return !clientMd5.equals(latest.getMd5());
            } finally {
                LOCK.unlock();
            }
        }
        // not locked, the client need to be updated.
        return true;
    }

    public ConfigData<MetaDataRegisterDto> fetchConfig() {
        return cache;
    }

    class DataChangeTask implements Runnable {

        /**
         * The Change time.
         */
        private final long changeTime = System.currentTimeMillis();

        @Override
        public void run() {
            if (clients.size() > httpSyncProperties.getNotifyBatchSize()) {
                List<LongPollingClient> targetClients = new ArrayList<>(clients.size());
                clients.drainTo(targetClients);
                List<List<LongPollingClient>> partitionClients = Lists.partition(targetClients, httpSyncProperties.getNotifyBatchSize());
                partitionClients.forEach(item -> scheduler.execute(() -> doRun(item)));
            } else {
                doRun(clients);
            }
        }

        private void doRun(final Collection<LongPollingClient> clients) {
            for (Iterator<LongPollingClient> iter = clients.iterator(); iter.hasNext();) {
                LongPollingClient client = iter.next();
                iter.remove();
                client.sendResponse("METADATA");
                log.info("send response with the changed metadata,ip={}, changeTime={}", client.ip, changeTime);
            }
        }
    }

    class LongPollingClient implements Runnable {

        /**
         * The Async context.
         */
        private final AsyncContext asyncContext;

        /**
         * The Ip.
         */
        private final String ip;

        /**
         * The Timeout time.
         */
        private final long timeoutTime;

        /**
         * The Async timeout future.
         */
        private Future<?> asyncTimeoutFuture;

        /**
         * Instantiates a new Long polling client.
         *
         * @param ac          the ac
         * @param ip          the ip
         * @param timeoutTime the timeout time
         */
        LongPollingClient(final AsyncContext ac, final String ip, final long timeoutTime) {
            this.asyncContext = ac;
            this.ip = ip;
            this.timeoutTime = timeoutTime;
        }

        @Override
        public void run() {
            try {
                this.asyncTimeoutFuture = scheduler.schedule(() -> {
                    clients.remove(LongPollingClient.this);
                    if (isChangedMetaData((HttpServletRequest) asyncContext.getRequest())) {
                        sendResponse("METADATA");
                    } else {
                        sendResponse("NO");
                    }
                }, timeoutTime, TimeUnit.MILLISECONDS);
                clients.add(this);
            } catch (Exception ex) {
                log.error("add long polling client error", ex);
            }
        }

        void sendResponse(String data) {
            // cancel scheduler
            if (null != asyncTimeoutFuture) {
                asyncTimeoutFuture.cancel(false);
            }
            generateResponse((HttpServletResponse) asyncContext.getResponse(), data);
            asyncContext.complete();
        }
    }
}
