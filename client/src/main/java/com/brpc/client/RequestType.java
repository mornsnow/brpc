package com.brpc.client;

/**
 * @author jianyang 2018/7/4
 */
public enum RequestType {
    GET_PARAM("获取参数"), EXECUTE_TEST("执行测试");

    RequestType(String msg) {
        this.msg = msg;
    }

    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
