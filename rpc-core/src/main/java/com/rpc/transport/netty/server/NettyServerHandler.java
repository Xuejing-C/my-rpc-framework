package com.rpc.transport.netty.server;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.handler.RpcRequestHandler;
import com.rpc.util.ThreadPoolFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * 自定义服务端 ChannelHandler
 * 处理客户端发来的消息
 * */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RpcRequestHandler rpcRequestHandler;
    private static final String THREAD_NAME_PREFIX = "netty-server-handler";
    private static final ExecutorService threadPool;

    static {
        rpcRequestHandler =new RpcRequestHandler();
        threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }

    // 当服务器接收到客服端发送的消息时，被调用
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        threadPool.execute(() -> {
            try {
                RpcRequest rpcRequest = (RpcRequest) msg;
                logger.info(String.format("server receive message: %s", rpcRequest));

                // 执行目标方法（客户端需要执行的方法）并且返回方法结果
                Object result = rpcRequestHandler.handle(rpcRequest);
                logger.info(String.format("server get result: %s", result.toString()));

                ChannelFuture f = ctx.writeAndFlush(RpcResponse.success(result, rpcRequest.getRequestId())); // 将响应消息写回客户端
                f.addListener(ChannelFutureListener.CLOSE); // 监听器，当响应消息写回后，关闭与客户端的连接
            } finally {
                // 用于释放 Netty 对象的引用计数，防止内存泄漏。msg是客户端发送的消息对象，在处理完消息后需要手动释放。
                ReferenceCountUtil.release(msg);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server catch exception", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
