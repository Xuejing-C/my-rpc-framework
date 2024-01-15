package com.rpc.netty.kyro.server;

import com.rpc.netty.kyro.dto.RpcRequest;
import com.rpc.netty.kyro.dto.RpcResponse;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 处理客户端消息
 * */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static final AtomicInteger atomicInteger = new AtomicInteger(1);

    // 当服务器接收到客服端发送的消息时，被调用
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcRequest rpcRequest = (RpcRequest) msg;
            // atomicInteger：接收的次数
            logger.info("server receive msg: [{}], times: [{}]", rpcRequest, atomicInteger.getAndIncrement());
            RpcResponse messageFromServer = RpcResponse.builder().message("message from server").build();
            ChannelFuture f = ctx.writeAndFlush(messageFromServer); // 将响应消息写回客户端
            f.addListener(ChannelFutureListener.CLOSE); // 监听器，当响应消息写回后，关闭与客户端的连接
        } finally {
            // 用于释放 Netty 对象的引用计数，防止内存泄漏。msg是客户端发送的消息对象，在处理完消息后需要手动释放。
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server catch exception", cause);
        ctx.close();
    }
}
