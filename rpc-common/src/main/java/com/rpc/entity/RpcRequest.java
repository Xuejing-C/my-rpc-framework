package com.rpc.entity;

import lombok.*;

import java.io.Serializable;

/**
 * 请求
 * */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RpcRequest implements Serializable {
    private String requestId; // 请求id
    private String interfaceName; // 目标接口
    private String methodName; // 目标方法
    private Object[] parameters; // 参数
    private Class<?>[] paramTypes; // 参数类型
    private Boolean heartBeat; // 是否心跳检测包
}
