package com.rpc.transport;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.transport.netty.client.NettyClient;
import com.rpc.transport.socket.client.SocketRpcClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * JDK动态代理
 * 当动态代理对象调用一个方法的时候，实际调用的是 invoke 方法。
 * */
@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private final RpcClient rpcClient;

    public RpcClientProxy(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    /**
     * 获取某个类的动态代理对象
     * */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * @param proxy: 动态生成的代理类
     * @param method: 原生方法
     * */
    // 在 Java 中，泛型提供了类型安全的操作，但是有些情况下会涉及到未检查的类型转换，可能会导致运行时的 ClassCastException。
    // 为了在这些情况下告诉编译器不生成警告，就可以使用 @SuppressWarnings("unchecked") 注解。
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("call invoke method and invoked method is: {}#{}", method.getDeclaringClass().getName(), method.getName());
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .heartBeat(false)
                .build();
        Object result = null;
        if (rpcClient instanceof NettyClient) {
            // Future 在实际使用过程中存在一些局限性比如不支持异步任务的编排组合、获取计算结果的 get() 方法为阻塞调用。
            CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcClient.sendRpcRequest(rpcRequest);
            try {
                // 获取异步调用的结果，阻塞等待直到结果返回
                result = completableFuture.get().getData();
            } catch (InterruptedException | ExecutionException e) {
                log.error("method call request failed to send", e);
                return null;
            }
        }
        if (rpcClient instanceof SocketRpcClient) {
            RpcResponse rpcResponse = (RpcResponse) rpcClient.sendRpcRequest(rpcRequest);
            result = rpcResponse.getData();
        }
        return result;
    }
}
