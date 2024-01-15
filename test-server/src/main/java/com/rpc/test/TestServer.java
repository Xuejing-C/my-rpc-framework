package com.rpc.test;

import com.rpc.api.HelloService;
import com.rpc.transport.socket.server.SocketRpcServer;

public class TestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        socketRpcServer.register(helloService, 9000);
    }
}
