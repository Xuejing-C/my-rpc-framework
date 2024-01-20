package com.rpc.transport.netty.server;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.factory.SingletonFactory;
import com.rpc.handler.RpcRequestHandler;
import com.rpc.factory.ThreadPoolFactory;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * 自定义服务端 ChannelHandler
 * 处理客户端发来的消息
 * */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private final RpcRequestHandler rpcRequestHandler;
    private static final String THREAD_NAME_PREFIX = "netty-server-handler";
    private final ExecutorService threadPool;

    public NettyServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
        this.threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }

    // 当服务器接收到客服端发送的消息时，被调用
    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        threadPool.execute(() -> {
            try {
                if (rpcRequest.getHeartBeat()) {
                    log.info("receive heart beat package from client");
                    return;
                }
                log.info(String.format("server receive message: %s", rpcRequest));

                // 执行目标方法（客户端需要执行的方法）并且返回方法结果
                Object result = rpcRequestHandler.handle(rpcRequest);
                log.info(String.format("server get result: %s", result.toString()));

                //ChannelFuture f = ctx.writeAndFlush(RpcResponse.success(result, rpcRequest.getRequestId())); // 将响应消息写回客户端
                //f.addListener(ChannelFutureListener.CLOSE); // 监听器，当响应消息写回后，关闭与客户端的连接
                if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                    ctx.writeAndFlush(RpcResponse.success(result, rpcRequest.getRequestId()));
                } else {
                    log.error("channel is not writable");
                }
            } finally {
                // 用于释放 Netty 对象的引用计数，防止内存泄漏。msg是客户端发送的消息对象，在处理完消息后需要手动释放。
                ReferenceCountUtil.release(rpcRequest);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception", cause);
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 处理空闲状态事件
     * */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                // 如果是读空闲事件
                log.info("heartbeat packet not received for a long time, disconnected...");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
