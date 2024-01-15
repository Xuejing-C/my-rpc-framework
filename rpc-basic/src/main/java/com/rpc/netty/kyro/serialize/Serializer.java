package com.rpc.netty.kyro.serialize;

public interface Serializer {
    byte[] serialize(Object obj);
    <T> T deserializer(byte[] bytes, Class<T> clazz);
}
