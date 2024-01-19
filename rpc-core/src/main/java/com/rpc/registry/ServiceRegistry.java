package com.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 * */
public interface ServiceRegistry {
    /**
     * 将一个服务实例注册进注册表
     * @param serviceName 服务(接口)名称
     * @param inetSocketAddress 提供服务的地址
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);
}
