package com.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RpcError {
    SERVICE_INVOCATION_FAILURE("service invocation failure"),
    SERVICE_CAN_NOT_BE_NULL("service can not be null");

    private final String message;
}
