package com.rpc.provider;

import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的服务表
 * */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider {
    /**
     * 保存接口名和服务的对应关系
     * key: service/interface name
     * value: service
     * */
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    @Override
    public <T> void addServiceProvider(T service, Class<T> serviceClass) {
        // getCanonicalName() 返回类的规范名称(全名)。例如, com.example.MyClass
        String serviceName = serviceClass.getCanonicalName();
        if (registeredService.contains(serviceName)) return;
        registeredService.add(serviceName);
        /*
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new RpcException(RpcError.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }
        for (Class<?> i : interfaces) {
            serviceMap.put(i.getCanonicalName(), service);
        }
        */
        serviceMap.put(serviceName, service);
        log.info("Add service {} to interfaces {}", serviceName, serviceClass.getInterfaces());
    }

    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (null == service) {
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service;
    }
}
