package com.brpc.client;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * brpc 的客户端bean,用于服务消费端初始化远程调用
 *
 * @author joe 2017/5/26 下午5:55
 * @version BrpcReferenceBean, v 0.0.1 2017/5/26 下午5:55 joe Exp $$
 */
// @Service
public class BrpcReferenceBean implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(BrpcReferenceBean.class);

    /**
     * 引用接口名称
     */
    private String serviceName;
    /**
     * 群组名称，每个服务必须要有
     */
    private String group;

    /**
     * service版本号,默认 1.0.0
     */
    private String version = "1.0.0";

    /**
     * 延迟注册时间
     */
    private int delay = 0;

    /**
     * 远程服务调用重试次数，不包括第一次调用，不需要重试请设为0
     */
    private int retries = 0;

    /**
     * 服务负责人，用于服务治理，请填写负责人公司邮箱前缀
     */
    private String owner;
    /**
     * 接口超时时间，默认 3s
     */
    private long timeout = 3 * 1000;

    private ApplicationContext context;
    private Class<?> interfaceClass = null;
    /**
     * 缓存远程调用的实例
     */
    private Object instance;

    @Autowired
    ClientInvoker invoker;

    @Override
    public void afterPropertiesSet() throws Exception {
        invoker.setServiceVersion(serviceName, group, version);
    }

    // @Bean
    public synchronized void init() {
        if (instance != null || !check()) {
            return;
        }

        String className = serviceName + "_impl";

        try {
            instance = Class.forName(className).newInstance();
            Method method = instance.getClass().getMethod("setInvoker", ClientInvoker.class);
            method.invoke(instance, invoker);
        } catch (Exception e) {
            logger.error("", e);
        }

    }

    /**
     * 检查配置
     *
     * @return
     */
    private boolean check() {
        if (StringUtils.isEmpty(serviceName) || StringUtils.isEmpty(group)) {
            logger.error("[brpc] the serviceName or group is null!!!,can't init bean");
            return false;
        }

        return true;
    }

    @Override
    public Object getObject() throws Exception {
        init();
        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        if (interfaceClass != null) {
            return interfaceClass;
        }
        try {
            if (StringUtils.isNoneBlank(serviceName)) {
                this.interfaceClass = Class.forName(serviceName, true, Thread.currentThread().getContextClassLoader());
            }
        } catch (ClassNotFoundException t) {
            throw new IllegalStateException(t.getMessage(), t);
        }
        return interfaceClass;
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

}
