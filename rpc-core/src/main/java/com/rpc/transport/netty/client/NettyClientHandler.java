package com.rpc.transport.netty.client;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.factory.SingletonFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 自定义客户端ChannelHandler
 * 处理服务端返回的消息
 * */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;

    public NettyClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }
    /**
     * 读取服务端传输的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcResponse rpcResponse = (RpcResponse) msg;
            log.info("client receive msg: [{}]", rpcResponse.toString());
            // 声明一个AttributeKey对象
            //AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse"+rpcResponse.getRequestId());
            // 将服务端的返回结果保存到AttributeMap上，AttributeMap可以视为一个Channel的共享数据源
           // ctx.channel().attr(key).set(rpcResponse);
           // ctx.channel().close();
            unprocessedRequests.complete(rpcResponse);
        } finally {
            // 用于释放 Netty 对象的引用计数，防止内存泄漏。msg是服务端发送的消息对象，在处理完消息后需要手动释放。
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 处理消息发生异常的时候被调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception", cause);
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 处理空闲状态事件
     * */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 判断触发的事件是否是空闲状态事件
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                // 如果是写空闲事件，表示在规定的时间内没有向服务器发送数据，就发送一个心跳包给服务器以保持连接。
                log.info("send heart beat package to [{}]", ctx.channel().remoteAddress());
                Channel channel = ChannelProvider.get((InetSocketAddress) ctx.channel().remoteAddress());
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setHeartBeat(true);
                // 添加一个监听器 ChannelFutureListener.CLOSE_ON_FAILURE，即如果发送失败则关闭连接。
                channel.writeAndFlush(rpcRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                // 如果是其他空闲事件，调用父类的处理方法
                super.userEventTriggered(ctx, evt);
            }
        }
    }
}
