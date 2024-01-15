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
    NOT_FOUND_METHOD(500, "Method not found"),
    NOT_FOUND_CLASS(500, "Class not found");

    private final Integer code;
    private final String message;
}
