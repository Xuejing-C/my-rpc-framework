package com.rpc.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.exception.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo序列化类，Kryo序列化效率很高，但是只兼容 Java 语言
 */
@Slf4j
public class KryoSerializer implements Serializer{

    // Kryo不是线程安全的，所以使用ThreadLocal存放每个线程的kryo对象
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
       Kryo kryo= new Kryo();
       kryo.register(RpcRequest.class);
       kryo.register(RpcResponse.class);
       kryo.setReferences(true); // 启用对象引用。关闭后可能存在序列化问题
       kryo.setRegistrationRequired(false); // 关闭后可以提高性能
       return kryo;
    });
    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, obj); // obj -> byte[]
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            log.error("occur exception when serialize:", e);
            throw new SerializeException("Serialization failed");
        }
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input= new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            Object o = kryo.readObject(input, clazz); // byte[] -> obj
            kryoThreadLocal.remove();
            return clazz.cast(o); // 强制类型转换
        } catch (Exception e) {
            log.error("occur exception when deserialize:", e);
            throw new SerializeException("Deserialization failed");
        }
    }
}
