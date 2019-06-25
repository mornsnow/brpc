package com.brpc.client;

import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import javax.net.ssl.SSLException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

/**
 * Utils的描述:<br>
 *
 * @author joe 2017/6/4 下午8:50
 * @version Utils, v 0.0.1 2017/6/4 下午8:50 joe Exp $$
 */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static String host;

    private static SslContext clientContext;

    public static InputStream loadInputStreamCert(String name) {
        return Utils.class.getClassLoader().getResourceAsStream("certs/" + name);
    }

    public static SslContext buildClientSslContext() {
        if (clientContext != null) {
            return clientContext;
        }
        try {
            InputStream certs = loadInputStreamCert("server.pem");
            clientContext = GrpcSslContexts.configure(SslContextBuilder.forClient().trustManager(certs)).build();
            return clientContext;
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    public static SslContext buildServerSslContext() {
        try {
            InputStream certs = loadInputStreamCert("server.pem");
            InputStream keys = loadInputStreamCert("server_pkcs8.key");
            return GrpcSslContexts.configure(SslContextBuilder.forServer(certs, keys)).build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取本机的name
     *
     * @return name
     */
    public static String getLocal() {
        if (StringUtils.isNoneBlank(host)) {
            return host;
        }
        try {
            /** 返回本地主机。 */
            InetAddress addr = InetAddress.getLocalHost();
            /** 返回 IP 地址字符串（以文本表现形式） */
            host = addr.getHostAddress();
        } catch (Exception ex) {
            host = "127.0.0.1";
        }
        return host;
    }


    public static boolean isDevelop() {
        try {
            String os = System.getProperty("os.name");
            return os.toLowerCase().contains("mac") || os.toLowerCase().contains("windows");
        } catch (Exception e) {

        }
        return false;
    }


    public static void setSpringApplicationName(SpringApplication app, String springApplicationName) {
        if (StringUtils.isEmpty(springApplicationName)) {
            return;
        }
        springApplicationName = isDevelop() ? springApplicationName + ".local" : springApplicationName;

        Properties p = new Properties();
        p.setProperty("spring.application.name", springApplicationName);

        app.setDefaultProperties(p);
    }


}
