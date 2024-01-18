package com.rpc.transport.netty.client;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.serializer.KryoSerializer;
import com.rpc.transport.RpcClient;
import com.rpc.transport.netty.codec.NettyKryoDecoder;
import com.rpc.transport.netty.codec.NettyKryoEncoder;
import com.rpc.util.RpcMessageChecker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NIO客户端
 * */
public class NettyClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private final String host;
    private final int port;
    private static final Bootstrap b; // 客户端启动引导类/辅助类

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // 初始化相关资源
    static {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        KryoSerializer kryoSerializer = new KryoSerializer();
        b = new Bootstrap();

        // 线程模型
        b.group(eventLoopGroup)
                // IO模型
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // 连接的超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                // 指定消息的处理对象
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        socketChannel.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        socketChannel.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     * */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 引用类型原子类，在多线程环境下安全地修改共享的引用对象。
        AtomicReference<Object> result = new AtomicReference<>(null);
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(00);
            // ChannelFuture f = b.connect(host, port).sync(); // 尝试建立连接
            // logger.info("client connect {}", host + ":" + port);
            Channel futureChannel = ChannelProvider.get(new InetSocketAddress(host, port));
            logger.info("send message");
            if (futureChannel.isActive()) {
                futureChannel.writeAndFlush(rpcRequest).addListener(future -> {
                    if (future.isSuccess()) {
                        logger.info("client send message: [{}]", rpcRequest.toString());
                    } else {
                        logger.error("send failed:", future.cause());
                    }
                });
                futureChannel.closeFuture().sync(); // 阻塞等待直到Channel关闭(先获取Channel的CloseFuture对象)
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse rpcResponse = futureChannel.attr(key).get();
                RpcMessageChecker.check(rpcResponse, rpcRequest);
                result.set(rpcResponse.getData());
            }
        } catch (InterruptedException e) {
            logger.error("occur exception when connect server:", e);
        }
        return result.get();
    }

    public static Bootstrap initializeBootstrap() {
        return b;
    }
}
