package com.rpc.test;

import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;
import com.rpc.transport.RpcClient;
import com.rpc.transport.RpcClientProxy;
import com.rpc.transport.socket.client.SocketRpcClient;

public class SocketTestClient {
    public static void main(String[] args) {
        RpcClient socketRpcClient = new SocketRpcClient("127.0.0.1", 9000);
        RpcClientProxy proxy = new RpcClientProxy(socketRpcClient);

        HelloService helloService = proxy.getProxy(HelloService.class); // 代理对象
        HelloObject object = new HelloObject(12, "This is a message");
        // 当代理对象调用方法时，实际调用到SocketRpcClientProxy类中的invoke()方法
        String res = helloService.hello(object);
        System.out.println(res);
    }
}