package com.brpc.client;

import org.springframework.context.annotation.Bean;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PollingServerListUpdater;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;

public class CustomLoadBalancer {
	@Bean
	public ILoadBalancer ribbonLoadBalancer(IClientConfig config, ServerList<Server> serverList,
			ServerListFilter<Server> serverListFilter, IRule rule, IPing ping) {
		return new ZoneAwareLoadBalancer<>(config, rule, ping, serverList, serverListFilter,
				new PollingServerListUpdater(config));
	}
}