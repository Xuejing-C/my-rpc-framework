package com.rpc.serializer;

/**
 * 序列化接口
 * */
public interface Serializer {
    byte[] serialize(Object obj);
    <T> T deserializer(byte[] bytes, Class<T> clazz);
}
