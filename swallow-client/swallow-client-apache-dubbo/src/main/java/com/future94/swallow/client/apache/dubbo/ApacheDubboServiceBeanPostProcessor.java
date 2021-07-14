package com.future94.swallow.client.apache.dubbo;

import com.future94.swallow.client.dubbo.common.annotation.SwallowDubboClient;
import com.future94.swallow.client.dubbo.common.disruptor.RegisterClientConsumerFactory;
import com.future94.swallow.client.dubbo.common.dto.DubboRpcExt;
import com.future94.swallow.common.disruptor.DisruptorProviderManage;
import com.future94.swallow.common.disruptor.provider.DisruptorProvider;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.dto.ServerConfig;
import com.future94.swallow.common.thread.SwallowThreadFactory;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author weilai
 */
@Slf4j
@SuppressWarnings("rawtypes")
public class ApacheDubboServiceBeanPostProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private DisruptorProvider<MetaDataRegisterDto> disruptorProvider;

    private String appName;

    private String contextPath;

    private final AtomicBoolean register = new AtomicBoolean(false);

    private final ExecutorService executorService;

    public ApacheDubboServiceBeanPostProcessor(ServerConfig serverConfig) {
        if (CollectionUtils.isEmpty(serverConfig.getServerList())) {
            throw new RuntimeException("config serverList must be not empty");
        }
        this.appName = serverConfig.getAppName();
        Optional.ofNullable(serverConfig.getContextPath()).ifPresent(c -> this.contextPath = c.startsWith("/") ? c : "/" + c);
        DisruptorProviderManage<MetaDataRegisterDto> disruptorProviderManage = new DisruptorProviderManage<>(new RegisterClientConsumerFactory(), serverConfig);
        disruptorProviderManage.startup();
        disruptorProvider = disruptorProviderManage.getProvider();
        executorService = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                SwallowThreadFactory.create("swallow-register-apache-dubbo-"));
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, ServiceBean> serviceBeanMap = event.getApplicationContext().getBeansOfType(ServiceBean.class);
        if (!serviceBeanMap.isEmpty() && register.compareAndSet(false, true)) {
            for (Map.Entry<String, ServiceBean> entry : serviceBeanMap.entrySet()) {
                executorService.execute(() -> this.handler(entry.getValue()));
            }
        }
    }

    private void handler(final ServiceBean serviceBean) {
        Class<?> clazz = serviceBean.getRef().getClass();
        if (AopUtils.isCglibProxy(clazz) || ClassUtils.isCglibProxyClass(clazz)) {
            String superClassName = clazz.getGenericSuperclass().getTypeName();
            try {
                clazz = Class.forName(superClassName);
            } catch (ClassNotFoundException e) {
                log.error(String.format("class not found: %s", superClassName));
                return;
            }
        }
        if (AopUtils.isJdkDynamicProxy(clazz)) {
            clazz = AopUtils.getTargetClass(serviceBean.getRef());
        }
        Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(clazz);
        for (Method method : methods) {
            SwallowDubboClient dubboClient = method.getAnnotation(SwallowDubboClient.class);
            if (Objects.nonNull(dubboClient) && dubboClient.enabled()) {
                doRegister(buildMetaDataDTO(serviceBean, dubboClient, method));
            }
        }
    }

    private MetaDataRegisterDto buildMetaDataDTO(final ServiceBean serviceBean, final SwallowDubboClient dubboClient, final Method method) {
        String appName = this.appName;
        if (StringUtils.isEmpty(appName)) {
            appName = serviceBean.getApplication().getName();
        }
        Class<?>[] parameterTypesClazz = method.getParameterTypes();
        String parameterTypes = Arrays.stream(parameterTypesClazz).map(Class::getName).collect(Collectors.joining(","));
        return MetaDataRegisterDto.builder()
                .appName(appName)
                .serviceName(serviceBean.getInterface())
                .methodName(method.getName())
                .contextPath(contextPath)
                .path(StringUtils.isNotEmpty(contextPath) ? contextPath + dubboClient.path() : dubboClient.path())
                .pathDesc(dubboClient.pathDesc())
                .parameterTypes(parameterTypes)
                .rpcExt(buildRpcExt(serviceBean))
                .enabled(dubboClient.enabled())
                .build();
    }

    private String buildRpcExt(final ServiceBean serviceBean) {
        DubboRpcExt build = DubboRpcExt.builder()
                .group(StringUtils.isNotEmpty(serviceBean.getGroup()) ? serviceBean.getGroup() : "")
                .version(StringUtils.isNotEmpty(serviceBean.getVersion()) ? serviceBean.getVersion() : "")
                .loadbalance(StringUtils.isNotEmpty(serviceBean.getLoadbalance()) ? serviceBean.getLoadbalance() : CommonConstants.DEFAULT_LOADBALANCE)
                .retries(Objects.isNull(serviceBean.getRetries()) ? CommonConstants.DEFAULT_RETRIES : serviceBean.getRetries())
                .timeout(Objects.isNull(serviceBean.getTimeout()) ? CommonConstants.DEFAULT_TIMEOUT : serviceBean.getTimeout())
                .build();
        return new Gson().toJson(build);
    }

    private void doRegister(MetaDataRegisterDto registerDto) {
        disruptorProvider.onData(swallowEvent -> swallowEvent.setData(registerDto));
    }
}
