package com.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.util.NacosUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Nacos服务注册
 */
@Slf4j
public class NacosServiceRegistry implements ServiceRegistry{
    /**
     * 服务注册
     * */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            NacosUtil.registerService(serviceName, inetSocketAddress);
            log.info("register service [{}] to Nacos, service address: {}:{}", serviceName, inetSocketAddress.getAddress(),
                    inetSocketAddress.getPort());
        } catch (NacosException e) {
            log.error("occur exception when register service: ", e);
            throw new RpcException(RpcError.SERVICE_REGISTRATION_FAILURE);
        }
    }
}
