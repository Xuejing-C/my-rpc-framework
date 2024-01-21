package com.rpc.test;

import com.rpc.annotation.ServiceScan;
import com.rpc.api.HelloService;
import com.rpc.transport.netty.server.NettyServer;

@ServiceScan
public class NettyTestServer {
    public static void main(String[] args) {
        // HelloService helloService = new HelloServiceImpl();
        NettyServer server = new NettyServer("127.0.0.1", 9999);
        server.start();
    }
}
