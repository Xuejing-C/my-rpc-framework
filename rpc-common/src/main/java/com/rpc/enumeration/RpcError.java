package com.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * RPC调用过程中的错误
 * */
@AllArgsConstructor
@Getter
public enum RpcError {
    SERVICE_INVOCATION_FAILURE("service invocation failure"),
    SERVICE_CAN_NOT_BE_NULL("service can not be null"),
    SERVICE_NOT_FOUND("service not found"),
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("service not implement any interface");

    private final String message;
}
