package com.rpc.test;

import com.rpc.api.HelloService;
import com.rpc.transport.RpcServer;
import com.rpc.transport.socket.server.SocketRpcServer;

public class SocketTestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl2();
        RpcServer socketRpcServer = new SocketRpcServer("127.0.0.1", 9998);
        socketRpcServer.publishService(helloService, HelloService.class);
    }
}
