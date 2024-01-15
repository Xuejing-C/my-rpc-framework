package com.rpc.netty.kyro.dto;

import lombok.*;

@NoArgsConstructor // Kryo不支持没有'无参构造函数'的对象进行反序列化
@AllArgsConstructor
@Getter // 不需要setter
@Builder // 构建器模式
@ToString
public class RpcRequest {
    private String interfaceName;
    private String methodName;
}
