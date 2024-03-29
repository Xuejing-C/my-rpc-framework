package com.rpc.entity;

import com.rpc.enumeration.ResponseCode;
import lombok.*;

import java.io.Serializable;

/**
 * 响应
 * */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {
    private String requestId; // 请求id
    private Integer code; // 响应状态码
    private String message; // 响应状态补充信息
    private T data; // 响应体

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(ResponseCode.SUCCESS.getCode());
        response.setMessage(ResponseCode.SUCCESS.getMessage());
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(ResponseCode code, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }
}
