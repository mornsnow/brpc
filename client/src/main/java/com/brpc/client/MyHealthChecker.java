package com.brpc.client;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jianyang 2018/12/26
 */
@Component
@RestController
public class MyHealthChecker implements HealthIndicator {

    private static boolean shutdown = false;

    @Override
    public Health health() {
        if (shutdown) {
            return new Health.Builder().withDetail("error", "shutdown for update").down().build();
        } else {
            return new Health.Builder().up().build();
        }
    }

    @RequestMapping("/myShutdown")
    public String myShutdown() {
        shutdown = true;
        return "bye bye ~";
    }

    @RequestMapping("/myCheck")
    public String myCheck() {
        return "ok";
    }


}