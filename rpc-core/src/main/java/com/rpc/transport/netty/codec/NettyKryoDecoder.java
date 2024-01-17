package com.rpc.transport.netty.codec;

import com.rpc.serializer.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 自定义解码器
 * 负责处理"入站"消息，从ByteBuf中读取字节序列，然后转换成业务对象。
 * */
@AllArgsConstructor
@Slf4j
public class NettyKryoDecoder extends ByteToMessageDecoder {
    private final KryoSerializer serializer;
    private final Class<?> genericClass;
    // Netty传输的消息长度(对象序列化后对应的字节数组长度)，存在ByteBuf头部
    private static final int BODY_LENGTH = 4;
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() >= BODY_LENGTH) {
            byteBuf.markReaderIndex(); // 标记当前readIndex的位置
            int dataLength = byteBuf.readInt();
            if (dataLength < 0 || byteBuf.readableBytes() < 0) {
                log.error("data length or byteBuf readableBytes is not valid");
                return;
            }
            // 消息不完整，重置readIndex到之前使用 markReaderIndex() 标记的位置，等待更多的数据到达。
            if (byteBuf.readableBytes() < dataLength) {
                byteBuf.resetReaderIndex();
                return;
            }
            byte[] body = new byte[dataLength];
            byteBuf.readBytes(body);
            Object obj = serializer.deserializer(body, genericClass);
            list.add(obj);
            log.info("successful decode ByteBuf to Object");
        }
    }
}
