package com.rpc.transport.netty.server;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.handler.RpcRequestHandler;
import com.rpc.registry.DefaultServiceRegistry;
import com.rpc.registry.ServiceRegistry;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义服务端 ChannelHandler
 * 处理客户端发来的消息
 * */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RpcRequestHandler rpcRequestHandler;
    private static ServiceRegistry serviceRegistry;

    static {
        rpcRequestHandler =new RpcRequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
    }

    // 当服务器接收到客服端发送的消息时，被调用
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcRequest rpcRequest = (RpcRequest) msg;
            logger.info(String.format("server receive message: %s", rpcRequest));

            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result = rpcRequestHandler.handle(rpcRequest, service);
            logger.info(String.format("server get result: %s", result.toString()));

            ChannelFuture f = ctx.writeAndFlush(RpcResponse.success(result, rpcRequest.getRequestId())); // 将响应消息写回客户端
            f.addListener(ChannelFutureListener.CLOSE); // 监听器，当响应消息写回后，关闭与客户端的连接
        } finally {
            // 用于释放 Netty 对象的引用计数，防止内存泄漏。msg是客户端发送的消息对象，在处理完消息后需要手动释放。
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server catch exception", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
