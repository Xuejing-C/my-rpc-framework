package com.rpc.test;

import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;
import com.rpc.entity.RpcResponse;
import com.rpc.transport.RpcClient;
import com.rpc.transport.RpcClientProxy;
import com.rpc.transport.netty.client.NettyClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyTestClient {
    public static void main(String[] args) {
        RpcClient client = new NettyClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "This is a message");
        String res = helloService.hello(object);
        System.out.println(res);
    }
}
