package com.rpc.test;

import com.rpc.annotation.ServiceScan;
import com.rpc.transport.RpcServer;
import com.rpc.transport.socket.server.SocketRpcServer;

@ServiceScan
public class SocketTestServer {
    public static void main(String[] args) {
        //HelloService helloService = new HelloServiceImpl2();
        RpcServer socketRpcServer = new SocketRpcServer("127.0.0.1", 9998);
        socketRpcServer.start();
    }
}
