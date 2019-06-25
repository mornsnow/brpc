package com.brpc.client;

/**
 * BindMessage的描述:<br>
 *
 * @author joe 2017/6/4 下午9:40
 * @version BindMessage, v 0.0.1 2017/6/4 下午9:40 joe Exp $$
 */
public interface BindMessage<R,P> {

    public R convert();

    public  P getInstance(R r);

}
