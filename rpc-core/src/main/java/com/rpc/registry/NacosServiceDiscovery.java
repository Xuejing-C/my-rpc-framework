package com.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.rpc.util.NacosUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Nacos服务发现
 * */
@Slf4j
public class NacosServiceDiscovery implements ServiceDiscovery{
    private final NamingService namingService;
    public NacosServiceDiscovery() {
        namingService = NacosUtil.getNamingService();
    }

    /**
     * 服务发现
     * */
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            List<Instance> instances = NacosUtil.getAllInstance(namingService, serviceName);
            Instance instance = instances.get(0);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            log.error("occur exception when look up service: ", e);
        }
        return null;
    }
}
