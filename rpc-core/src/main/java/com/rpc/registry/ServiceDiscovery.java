package com.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {
    /**
     * 根据服务名称查询服务地址
     * @param serviceName 服务名称
     * @return 服务地址
     */
    InetSocketAddress lookupService(String serviceName);
}
