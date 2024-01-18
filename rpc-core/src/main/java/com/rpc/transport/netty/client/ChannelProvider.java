package com.rpc.transport.netty.client;

import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 用于获取 Channel 对象
 */
@Slf4j
public class ChannelProvider {
    private static EventLoopGroup eventLoopGroup;
    private static Bootstrap bootstrap = NettyClient.initializeBootstrap();
    private static final int MAX_RETRY_COUNT = 5;
    private static Channel channel = null;

    public static Channel get(InetSocketAddress inetSocketAddress) {
        // CountDownLatch 允许 count 个线程阻塞在一个地方，直至所有线程的任务都执行完毕。
        // 当调用 await() 方法的时候，如果 state 不为 0，那就证明任务还没有执行完毕，await() 方法就会一直阻塞.
        // 直到count 个线程调用了countDown()使 state值被减为 0，或者调用await()的线程被中断，该线程才会从阻塞中被唤醒，执行后面的语句。
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            connect(bootstrap, inetSocketAddress, countDownLatch);
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("occur exception when get channel:", e);
        }
        return channel;
    }

    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, CountDownLatch countDownLatch) {
        connect(bootstrap, inetSocketAddress, countDownLatch, MAX_RETRY_COUNT);
    }

    /**
     * 带有重试机制的连接方法
     */
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, CountDownLatch countDownLatch, int retry) {
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("connection successful");
                channel = future.channel();
                countDownLatch.countDown();
                return;
            }
            if (retry == 0) {
                log.error("connection failed: retry chance runs out, quit connection");
                countDownLatch.countDown();
                throw new RpcException(RpcError.CLIENT_CONNECT_SERVER_FAILURE);
            }
            int order = (MAX_RETRY_COUNT - retry) + 1; // 第几次重连
            int delay = 1 << order; // 本次重连的间隔，2的order次方
            log.error("{}: connection failed, no.{} reconnection", new Date(), order);
            bootstrap.config().group().schedule(() ->
                    connect(bootstrap, inetSocketAddress, countDownLatch, retry - 1), delay, TimeUnit.SECONDS);
        });
    }
}
