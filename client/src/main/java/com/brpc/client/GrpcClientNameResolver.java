package com.brpc.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

public class GrpcClientNameResolver extends NameResolver {

	  private final String name;
	  private Listener listener;
	  private String host;
	  private int port;

	  public GrpcClientNameResolver(String name, String host, int port) {
	    this.name = name;
	    this.host = host;
	    this.port = port;
	  }

	  @Override
	  public String getServiceAuthority() {
		  return name;
	  }

	  @Override
	  public void start(Listener listener) {
	    this.listener = listener;
	    refresh();
	  }

	  @Override
	  public void refresh() {
		  SocketAddress sock = new InetSocketAddress(host, port);
		  EquivalentAddressGroup servers = new EquivalentAddressGroup(sock);
		  listener.onAddresses(Collections.singletonList(servers), Attributes.EMPTY);
	  }

	  @Override
	  public void shutdown() {
	  }
	}
