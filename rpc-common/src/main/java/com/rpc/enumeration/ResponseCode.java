package com.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码
 * */
@AllArgsConstructor
@Getter
public enum ResponseCode {
    SUCCESS(200, "The remote call is successful"),
    FAIL(500, "The remote call is failed"),
    METHOD_NOT_FOUND(500, "Method not found"),
    CLASS_NOT_FOUND(500, "Class not found");

    private final int code;
    private final String message;
}
