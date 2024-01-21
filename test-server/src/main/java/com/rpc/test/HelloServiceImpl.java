package com.rpc.test;

import com.rpc.annotation.Service;
import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(HelloObject helloObject) {
        log.info("Receive message: {}.", helloObject.getMessage());
        return "Receiver message from Netty service";
    }
}
