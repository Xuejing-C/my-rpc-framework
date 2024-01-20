package com.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 轮询负载均衡
 * */
@Slf4j
public class RoundRobinLoadBalancer implements LoadBalancer{
    private int index = 0;
    @Override
    public Instance select(List<Instance> instances) {
        log.info("utilize round load balancer");
        if(index >= instances.size()) {
            index %= instances.size();
        }
        return instances.get(index++);
    }
}
