package com.rpc.entity;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class RpcRequest implements Serializable {
    private String interfaceName; // 目标接口
    private String methodName; // 目标方法
    private Object[] parameters; // 参数
    private Class<?>[] paramTypes; // 参数类型

}
