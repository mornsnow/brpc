package com.brpc.client;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utils的描述:<br>
 */
@Component
public class SettingUtils implements InitializingBean {
    //daily和online环境zipkin地址
    public static String TRACE_HOST;

    public static String TRACE_API;

    public static String APP_NAME;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${dailyZipkinIp:10.42.246.109}")
    private String dailyZipkinIp;

    @Value("${prodZipkinIp:10.42.22.70}")
    private String prodZipkinIp;

    public static final int TRACE_PORT = 9411;

    public static boolean isDevelop() {
        try {
            String os = System.getProperty("os.name");
            return os.toLowerCase().contains("mac") || os.toLowerCase().contains("windows");
        } catch (Exception e) {

        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (isDevelop()) {
            TRACE_HOST = dailyZipkinIp;
        } else {
            TRACE_HOST = prodZipkinIp;
        }
        TRACE_API = "http://" + TRACE_HOST + ":" + SettingUtils.TRACE_PORT + "/api/v1/spans";
        if (appName.contains(":")) {
            APP_NAME = appName.split(":")[0];
        } else {
            APP_NAME = appName;
        }
    }
}
