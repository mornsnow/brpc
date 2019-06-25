package com.brpc.client;

import java.net.URI;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

/**
 * 提供grpc 的nameresolver grpc必须要匹配这个才能解析泛ip
 */
public class GrpcNameResolverProvider extends NameResolverProvider {

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 0;
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
        String host = targetUri.getHost();
        int port = targetUri.getPort();
        return new GrpcClientNameResolver("grpc", host, port);
    }

    @Override
    public String getDefaultScheme() {
        return "";
    }

}
