package com.rpc.test;

import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloServiceImpl2 implements HelloService {

    @Override
    public String hello(HelloObject helloObject) {
        log.info("Receive message: {}.", helloObject.getMessage());
        return "Receiver message from Socket service";
    }
}
