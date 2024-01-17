package com.rpc.transport.netty.client;

import com.rpc.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义客户端ChannelHandler
 * 处理服务端返回的消息
 * */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * 读取服务端传输的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcResponse rpcResponse = (RpcResponse) msg;
            log.info("client receive msg: [{}]", rpcResponse.toString());
            // 声明一个AttributeKey对象
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse"+rpcResponse.getRequestId());
            // 将服务端的返回结果保存到AttributeMap上，AttributeMap可以视为一个Channel的共享数据源
            ctx.channel().attr(key).set(rpcResponse);
            ctx.channel().close();
        } finally {
            // 用于释放 Netty 对象的引用计数，防止内存泄漏。msg是服务端发送的消息对象，在处理完消息后需要手动释放。
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 处理客户端消息发生异常的时候被调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
