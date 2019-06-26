package com.brpc.client;

public interface BindMessage<R, P> {

    public R convert();

    public P getInstance(R r);

}
