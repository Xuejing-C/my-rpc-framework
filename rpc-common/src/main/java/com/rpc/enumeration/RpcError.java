package com.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * RPC调用过程中的错误
 * */
@AllArgsConstructor
@Getter
public enum RpcError {
    CLIENT_CONNECT_SERVER_FAILURE("cilent connect server failure"),
    SERVICE_INVOCATION_FAILURE("service invocation failure"),
    SERVICE_CAN_NOT_BE_NULL("service can not be null"),
    SERVICE_NOT_FOUND("service not found"),
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("service not implement any interface"),
    REQUEST_NOT_MATCH_RESPONSE("request and response not match"),
    SERVICE_REGISTRY_CONNECTION_FAILURE("service registry connection failure"),
    SERVICE_REGISTRATION_FAILURE("service registration failure");

    private final String message;
}
