package com.rpc.netty.kyro.coder;

import com.rpc.netty.kyro.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * 自定义编码器
 * 负责处理"出站"消息，将消息格式转换为字节数组然后写入字节数据容器ByteBuf对象中。
 * */
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {
    private final Serializer serializer;
    private final Class<?> genericClass; // 类型信息是在运行时动态确定的，使得编码器可以处理不同类型的对象
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            byte[] body = serializer.serialize(o);
            int dataLength = body.length;
            // dataLength本身占4个字节
            byteBuf.writeInt(dataLength);
            byteBuf.writeBytes(body);
        }
    }
}
