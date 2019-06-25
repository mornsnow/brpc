package com.brpc.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EurekaHealthCheckHandlerConfiguration {
 
    @Autowired(required = false)
    private HealthAggregator healthAggregator = new OrderedHealthAggregator();
 
    @Bean
    @ConditionalOnMissingBean
    public EurekaHealthCheckHandler eurekaHealthCheckHandler() {
        return new EurekaHealthCheckHandler(healthAggregator);
    }
}
