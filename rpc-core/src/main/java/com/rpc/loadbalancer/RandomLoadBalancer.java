package com.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡
 * */
@Slf4j
public class RandomLoadBalancer implements LoadBalancer{
    @Override
    public Instance select(List<Instance> instances) {
        log.info("utilize random load balancer");
        return instances.get(new Random().nextInt(instances.size()));
    }
}
