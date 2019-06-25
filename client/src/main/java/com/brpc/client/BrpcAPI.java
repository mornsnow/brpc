package com.brpc.client;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jianyang 2018/12/26
 */
@Component
@RestController
public class BrpcAPI {


    @PostMapping("/getEnv")
    public String getEnv(String key) {
        return SpringPropertyReader.getProperty(key, "not setting");
    }


}