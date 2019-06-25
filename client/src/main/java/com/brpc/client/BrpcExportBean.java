package com.brpc.client;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.netty.NettyServerBuilder;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * BrpcExportBean的描述:<br>
 *
 * @author joe 2017/6/4 下午8:17
 * @version BrpcExportBean, v 0.0.1 2017/6/4 下午8:17 joe Exp $$
 */
public class BrpcExportBean implements DisposableBean, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(BrpcExportBean.class.getName());

    private Object ref;

    private Map<String, Object> services = new HashMap<>();
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

    private Server server;
    @Value("${server.port}")
    private int port;

    @Value("${spring.application.name}")
    private String appName;

    @Override
    public void afterPropertiesSet() throws Exception {

        logger.info("GRPC Service is starting.");

        int port = this.port + 1;
        NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port).sslContext(Utils.buildServerSslContext());

        services.forEach((serviceName, entry) -> {
            try {
                BindableService instance = (BindableService) ClassUtils.getClass(serviceName
                        + "_service").newInstance();
                Method method = instance.getClass().getMethod("setInvoker", ClassUtils.getClass(serviceName));
                method.invoke(instance, entry);

                serverBuilder.addService(ServerInterceptors.intercept(instance, new ServerLogInterceptor()));
            } catch (Exception e) {
                logger.error("", e);
            }

        });
        server = serverBuilder.build().start();
        logger.info("GRPC Service started.");
    }

    public void setServices(Map<String, Object> services) {
        this.services = services;
    }

    @Override
    public void destroy() throws Exception {
        server.shutdown();
        logger.warn("GRPC Service stopped.");
    }

}
