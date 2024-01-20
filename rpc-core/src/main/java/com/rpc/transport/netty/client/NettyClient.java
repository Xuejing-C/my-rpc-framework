package com.rpc.transport.netty.client;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.registry.NacosServiceDiscovery;
import com.rpc.registry.ServiceDiscovery;
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
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NIO客户端
 * */
@Slf4j
public class NettyClient implements RpcClient {
    private static final  EventLoopGroup eventLoopGroup;
    private static final Bootstrap b; // 客户端启动引导类/辅助类
    private final ServiceDiscovery serviceDiscovery;

    public NettyClient() {
        this.serviceDiscovery = new NacosServiceDiscovery();
    }

    // 初始化相关资源
    static {
        eventLoopGroup = new NioEventLoopGroup();
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
                        // IdleStateHandler 是 Netty 提供的处理空闲状态的处理器，用于检测连接的空闲时间，触发相应的空闲事件。
                        // 第一个参数（readerIdleTime）：读取空闲时间，表示在指定的时间间隔内没有从连接中读取到数据时，触发 READER_IDLE 事件。
                        // 第二个参数（writerIdleTime）：写入空闲时间，表示在指定的时间间隔内没有向连接中写入数据时，触发 WRITER_IDLE 事件。
                        // 第三个参数（allIdleTime）：所有类型的空闲时间，表示在指定的时间间隔内既没有读取到数据也没有写入数据时，触发 ALL_IDLE 事件。
                        socketChannel.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        socketChannel.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        socketChannel.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        socketChannel.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    /**
     * 发送消息到服务端
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     * */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 引用类型原子类，在多线程环境下安全地修改共享的引用对象。
        AtomicReference<Object> result = new AtomicReference<>(null);
        try {
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            Channel futureChannel = ChannelProvider.get(inetSocketAddress);
            log.info("send message");
            if (futureChannel.isActive()) {
                futureChannel.writeAndFlush(rpcRequest).addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("client send message: [{}]", rpcRequest.toString());
                    } else {
                        log.error("send failed:", future.cause());
                    }
                });
                futureChannel.closeFuture().sync(); // 阻塞等待直到Channel关闭(先获取Channel的CloseFuture对象)
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse rpcResponse = futureChannel.attr(key).get();
                RpcMessageChecker.check(rpcResponse, rpcRequest);
                result.set(rpcResponse.getData());
            } else {
                futureChannel.close();
                eventLoopGroup.shutdownGracefully();
                System.exit(0);
            }
        } catch (InterruptedException e) {
            log.error("occur exception when connect server:", e);
            Thread.currentThread().interrupt();
        }
        return result.get();
    }

    public static Bootstrap initializeBootstrap() {
        return b;
    }
}
