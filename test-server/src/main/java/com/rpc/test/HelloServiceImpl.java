package com.rpc.test;

import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(HelloObject helloObject) {
        log.info("Receive message: {}.", helloObject.getMessage());
        String result = "Return content is " + helloObject.getId();
        log.info("Return message: {}.", result);
        return result;
    }
}
