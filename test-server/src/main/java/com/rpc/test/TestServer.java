package com.rpc.test;

import com.rpc.api.HelloService;
import com.rpc.registry.DefaultServiceRegistry;
import com.rpc.registry.ServiceRegistry;
import com.rpc.transport.socket.server.SocketRpcServer;

public class TestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        // 手动注册
        serviceRegistry.register(helloService);
        SocketRpcServer socketRpcServer = new SocketRpcServer(serviceRegistry);
        socketRpcServer.start(9000);
    }
}
