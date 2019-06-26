package com.brpc.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BrpcApiInterceptor implements HandlerInterceptor {

    /**
     * 配置brpcApi是否开放
     *
     * 可根据服务部署安全性要求，选择是否开放
     */
    @Value("${activeBrpcApi:false}")
    public boolean activeBrpcApi;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        if (!activeBrpcApi) {
            throw new Exception("未开放配置，访问失败");
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
