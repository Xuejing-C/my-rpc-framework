package com.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册中心接口
 * */
public interface ServiceRegistry {
    /**
     * 将一个服务注册进注册表
     * @param serviceName 服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);

    /**
     * 根据服务名称查询服务地址
     * @param serviceName 服务名称
     * @return 服务地址
     */
    InetSocketAddress lookupService(String serviceName);
}
