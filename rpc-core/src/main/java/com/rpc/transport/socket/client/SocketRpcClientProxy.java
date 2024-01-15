package com.rpc.transport.socket.client;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK动态代理
 * */
public class SocketRpcClientProxy implements InvocationHandler {

    private String host;
    private int port;

    public SocketRpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 获取代理对象
     * */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * @param proxy: 动态生成的代理类
     * @param method: 原生方法
     * */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        SocketRpcClient socketRpcClient = new SocketRpcClient();
        return socketRpcClient.sendRpcRequest(rpcRequest, host, port);
    }
}
