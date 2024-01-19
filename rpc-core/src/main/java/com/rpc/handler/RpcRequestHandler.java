package com.rpc.handler;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.enumeration.ResponseCode;
import com.rpc.provider.ServiceProvider;
import com.rpc.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * rpcRequest 处理器
 * */
@Slf4j
public class RpcRequestHandler {
    private static final ServiceProvider serviceProvider;
    static {
        serviceProvider = new ServiceProviderImpl();
    }

    /**
     * 获取服务实例 & 获取方法执行结果
     * */
    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 执行目标方法
     * */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("successfully invoke method [{}] in service [{}]", rpcRequest.getMethodName(), rpcRequest.getInterfaceName());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
        }
        return result;
    }
}
