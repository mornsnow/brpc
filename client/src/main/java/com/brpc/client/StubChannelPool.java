package com.brpc.client;

import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StubChannelPool的描述:<br>
 *
 * @author joe 2017/6/7 下午12:07
 * @version StubChannelPool, v 0.0.1 2017/6/7 下午12:07 joe Exp $$
 */
public class StubChannelPool extends BasePooledObjectFactory {

    private static final Logger logger = LoggerFactory.getLogger(StubChannelPool.class);

    private String host;
    private int port;


    private String traceHost;

    private int tracePort;

    private String clientName;
    private static Object lock = new Object();

    private static Map<String, GenericObjectPool<Channel>> objectPool = new ConcurrentHashMap<>();

    private static GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

    static {
        /** 连接池的配置 */
        /** 下面的配置均为默认配置,默认配置的参数可以在BaseObjectPoolConfig中找到 */
        poolConfig.setMaxTotal(20); // 池中的最大连接数
        poolConfig.setMinIdle(8); // 最少的空闲连接数
        poolConfig.setMaxIdle(10); // 最多的空闲连接数
        poolConfig.setMaxWaitMillis(-1); // 当连接池资源耗尽时,调用者最大阻塞的时间,超时时抛出异常 单位:毫秒数
        poolConfig.setLifo(true); // 连接池存放池化对象方式,true放在空闲队列最前面,false放在空闲队列最后
        poolConfig.setMinEvictableIdleTimeMillis(1000L * 60L * 30L); // 连接空闲的最小时间,达到此值后空闲连接可能会被移除,默认即为30分钟
        poolConfig.setBlockWhenExhausted(true); // 连接耗尽时是否阻塞,默认为true
    }

    /**
     * 获取连接池
     *
     * @param host
     * @param port
     * @param instanceName
     * @return
     */
    public static Channel get(String host, int port, String instanceName, String traceHost, int tracePort, String clientName) {
        GenericObjectPool<Channel> genericObjectPool = objectPool.get(instanceName);

        if (genericObjectPool == null) {
            // 锁住对象，确保创建线程池合法
            synchronized (lock) {
                genericObjectPool = objectPool.get(instanceName);
                if (genericObjectPool == null) {
                    genericObjectPool = new GenericObjectPool<>(new StubChannelPool(host, port, traceHost, tracePort, clientName), poolConfig);
                    objectPool.put(instanceName, genericObjectPool);
                }
            }
        }

        try {
            Channel obj = genericObjectPool.borrowObject();
            return obj;
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    public static void returnObject(String instanceName, Object obj) {
        GenericObjectPool genericObjectPool = objectPool.get(instanceName);
        genericObjectPool.returnObject(obj);
    }

    public static void remove(String instanceName) {
        objectPool.remove(instanceName);
    }

    public StubChannelPool(String host, int port, String traceHost, int tracePort, String clientName) {
        this.host = host;
        this.port = port;
        this.traceHost = traceHost;
        this.tracePort = tracePort;
        this.clientName = clientName;
    }

    @Override
    public Channel create() throws Exception {
        URI uri = URI.create("https://" + host + ":" + port);

        ManagedChannelBuilder channelBuilder = NettyChannelBuilder.forTarget(uri.toString())
                // Channels are secure by default (via
                // SSL/TLS). For the example we disable TLS to
                // avoid
                // needing certificates.
                .nameResolverFactory(new GrpcNameResolverProvider())
                //
                .usePlaintext(false)
                //
                .negotiationType(NegotiationType.TLS)
                // 20M
                .maxInboundMessageSize(20971520)
                //从默认8K改为80K
                .maxHeaderListSize(81920)
                .sslContext(Utils.buildClientSslContext());
        Channel channel = channelBuilder.build();
        return ClientInterceptors.intercept(channel, new ClientLogInterceptor());
        // blockingStub = stub.invoke(null, channel);
        // return blockingStub;
    }

    @Override
    public PooledObject wrap(Object obj) {
        return new DefaultPooledObject(obj);
    }
}
