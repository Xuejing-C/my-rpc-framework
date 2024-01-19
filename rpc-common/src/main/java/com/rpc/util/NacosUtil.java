package com.rpc.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 管理Nacos连接
 * */
@Slf4j
public class NacosUtil {
    // Nacos服务器地址
    private static final String SERVER_ADDR = "127.0.0.1:8848";
    // 与Nacos注册中心进行通信
    private static final NamingService namingService;
    // 注册到 Nacos 的服务集合
    private static final Set<String> serviceNames = new HashSet<>();
    //
    private static InetSocketAddress address;

    static {
        namingService = getNamingService();
    }

    /**
     * 获取Nacos的NamingService实例
     * @return NamingService实例
     */
    public static NamingService getNamingService() {
        try {
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            log.error("occur exception when connect Nacos server: ", e);
            throw new RpcException(RpcError.SERVICE_REGISTRY_CONNECTION_FAILURE);
        }
    }

    /**
     * 注册服务实例到Nacos
     * @param serviceName 服务名
     * @param inetSocketAddress 当前 服务实例的地址
     */
    public static void registerService(String serviceName, InetSocketAddress inetSocketAddress) throws NacosException {
        namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        NacosUtil.address = inetSocketAddress;
        serviceNames.add(serviceName);
    }

    /**
     * 获取指定服务的所有实例信息
     * @param serviceName 服务名
     * @return 服务的所有实例信息列表
     */
    public static List<Instance> getAllInstance(String serviceName) throws NacosException {
        return namingService.getAllInstances(serviceName);
    }

    /**
     * 清除所有服务实例
     */
    public static void clearService() {
        if (!serviceNames.isEmpty() && address != null) {
            String host = address.getHostName();
            int port = address.getPort();
            for (String serviceName : serviceNames) {
                try {
                    namingService.deregisterInstance(serviceName, host, port);
                } catch (NacosException e) {
                    log.error("failed to deregister service {}", serviceName, e);
                }
            }
        }
    }
}
