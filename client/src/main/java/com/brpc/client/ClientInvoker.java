package com.brpc.client;

import com.mornsnow.starter.log.Const;
import com.mornsnow.starter.log.LogUtil;
import com.mornsnow.starter.log.QcLog;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GrpcInvoker的描述:<br>
 *
 * @author joe 2017/6/1 上午1:17
 * @version ClientInvoker, v 0.0.1 2017/6/1 上午1:17 joe Exp $$
 */
@Component
public class ClientInvoker implements InitializingBean {

    private static Map<String, String> serviceRegisterCaches = new ConcurrentHashMap<>();

    private static Map<String, Method> serviceStubCaches = new ConcurrentHashMap<>();
    private static Map<String, Method> serviceMethodCaches = new ConcurrentHashMap<>();
    private static Map<String, BindMessage> pojoCaches = new ConcurrentHashMap<>();

    @Autowired
    private LoadBalancerClient loadBalancer;

    @Autowired
    private DiscoveryClient client;

    private static final QcLog logger = LogUtil.getLogger(ClientInvoker.class);


    @Value("${spring.application.name}")
    private String clientName;

    public <R> R invoke(String serviceMethod, Object param) {

        String[] args = StringUtils.split(serviceMethod, ":");
        String serviceName = args[0];
        String methodName = args[1];

        long now = System.currentTimeMillis();

        String resisterInstanceName = serviceRegisterCaches.get(serviceName);
        if (StringUtils.isEmpty(resisterInstanceName)) {
            logger.error("brpc init service error, registerInstanceName is empty, serviceName:{}", serviceName);
            logger.trace("GRPC_CLIENT", Const.TRACE_TAG_ERROR, serviceName + "." + methodName, now, now);
        }
        ServiceInstance instance = loadBalancer.choose(resisterInstanceName);

        if (instance == null) {
            logger.error("brpc init reference service error :" + resisterInstanceName);
            logger.trace("GRPC_CLIENT", Const.TRACE_TAG_ERROR, serviceName + "." + methodName, now, now);
            return null;
        }

        String host = instance.getHost();
        int port = instance.getPort() + 1;

        // 建立通道
        String instanceName = instance.getHost() + ":" + instance.getPort();
        Method stub = getStubMethod(serviceName);
        Channel channel = StubChannelPool.get(host, port, instanceName, SettingUtils.TRACE_HOST, SettingUtils.TRACE_PORT, clientName);

        try {
            Object blockingStub = stub.invoke(null, channel);
            if (blockingStub == null) {
                return null;
            }
            Method funMethod = serviceMethodCaches.get(serviceMethod);
            if (funMethod == null) {
                funMethod = blockingStub.getClass().getMethod(methodName, param.getClass());
                serviceMethodCaches.put(serviceMethod, funMethod);
            }
            logger.info("channel " + (System.currentTimeMillis() - now) + "ms");
            now = System.currentTimeMillis();
            Object result = funMethod.invoke(blockingStub, param);
            long current = System.currentTimeMillis();
            logger.trace("GRPC_CLIENT", Const.TRACE_TAG_SUCCESS, serviceName + "." + methodName, now, current);
            logger.info("invoke " + (System.currentTimeMillis() - now) + "ms");

            String[] classNames = StringUtils.split(result.getClass().getName(), '$');
            String className = classNames[0].toLowerCase() + "." + classNames[1];
            BindMessage message = pojoCaches.get(className);
            if (message == null) {
                message = (BindMessage) Class.forName(className).newInstance();
                pojoCaches.put(className, message);
            }
            return (R) message.getInstance(result);
        } catch (StatusRuntimeException e) {
            StubChannelPool.remove(instanceName);
            return invoke(serviceMethod, param);
        } catch (Exception e) {
            logger.error("invoke " + serviceMethod + ":" + resisterInstanceName + " by ip:" + host + " port:" + port
                    + " error!", e);
            logger.trace("GRPC_CLIENT", Const.TRACE_TAG_ERROR, serviceName + "." + methodName, now, System.currentTimeMillis());

            throw new RuntimeException(e);
        } finally {
            StubChannelPool.returnObject(instanceName, channel);
        }
    }

    private Method getStubMethod(String serviceName) {
        Method stub = serviceStubCaches.get(serviceName);
        if (stub == null) {
            try {
                Class clazz = ClassUtils.getClass(serviceName + "Grpc");
                stub = clazz.getMethod("newBlockingStub", Channel.class);
                serviceStubCaches.put(serviceName, stub);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return stub;
    }

    /**
     * 设置service的version
     *
     * @param key
     * @param version
     */
    public void setServiceVersion(String key, String group, String version) {
        serviceRegisterCaches.put(key, group + ":" + version);
    }

    // @Bean
    // CommandLineRunner runner(DiscoveryClient dc) {
    // return args -> {
    // dc.getServices()
    // };
    // }

    @Override
    public void afterPropertiesSet() throws Exception {

        client.getServices().forEach(serviceId -> {
            List<ServiceInstance> serviceInstances = client.getInstances(serviceId);
            logger.info("eureka get services:" + serviceInstances + " " + serviceId);
        });

        logger.info("init ClientInvoker");
    }

}
