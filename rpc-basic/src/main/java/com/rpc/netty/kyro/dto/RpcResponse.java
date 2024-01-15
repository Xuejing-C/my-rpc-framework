package com.rpc.netty.kyro.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
public class RpcResponse {
    private String message;
}
