package com.rpc.netty.kyro.client;

import com.rpc.netty.kyro.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理服务端消息
 * */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcResponse rpcResponse = (RpcResponse) msg;
            log.info("client receive msg: [{}], times: [{}]", rpcResponse.toString());
            // 声明一个AttributeKey对象
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            // 将服务端的返回结果保存到AttributeMap上，AttributeMap可以视为一个Channel的共享数据源
            ctx.channel().attr(key).set(rpcResponse);
            ctx.channel().close();
        } finally {
            // 用于释放 Netty 对象的引用计数，防止内存泄漏。msg是服务端发送的消息对象，在处理完消息后需要手动释放。
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception", cause);
        ctx.close();
    }
}
