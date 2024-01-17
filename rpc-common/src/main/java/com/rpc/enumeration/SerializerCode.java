package com.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 序列化和反序列化器标识
 * */
@AllArgsConstructor
@Getter
public enum SerializerCode {
    KRYO(0);
    private final int code;
}
