package com.rpc.netty.kyro.server;

import com.rpc.netty.kyro.coder.NettyKryoDecoder;
import com.rpc.netty.kyro.coder.NettyKryoEncoder;
import com.rpc.netty.kyro.dto.RpcRequest;
import com.rpc.netty.kyro.dto.RpcResponse;
import com.rpc.netty.kyro.serialize.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private final int port;

    private NettyServer(int port) {
        this.port = port;
    }

    private void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // 处理客户端的TCP连接请求
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 用于具体的IO处理
        KryoSerializer kryoSerializer = new KryoSerializer();
        try {
            ServerBootstrap b = new ServerBootstrap(); // 服务端启动引导类
            // 配置线程组，指定线程模型(主从多线程模型)
            b.group(bossGroup, workerGroup)
                    // 日志打印
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 指定IO模型
                    . channel(NioServerSocketChannel.class)
                    // TCP默认开启Nagle算法，该算法的作用是尽可能的发送大数据块，减少网络传输
                    // TCP_NODELAY参数的作用是控制是否启用Nagle算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 开启TCP底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 表示系统用于临时存放已完成三次握手的请求队列的最大长度。如果建立连接频繁，服务器处理创建新连接较慢，可以适当调大此参数。
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 指定服务端消息的业务处理逻辑对象: NettyKryoDecoder, NettyKryoEncoder, NettyServerHandler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class));
                            socketChannel.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class));
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync(); // 绑定端口，调用sync方法阻塞直到绑定完成
            f.channel().closeFuture().sync(); // 阻塞等待直到服务器Channel关闭(先获取Channel的CloseFuture对象)
        } catch (InterruptedException e) {
            logger.error("occur exception when start server:", e);
        } finally {
            // 关闭线程组资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyServer(8889).run();
    }
}
