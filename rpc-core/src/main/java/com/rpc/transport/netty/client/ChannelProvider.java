package com.rpc.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 存储和获取 Channel 对象
 */
@Slf4j
public class ChannelProvider {
    private static Bootstrap bootstrap = NettyClient.initializeBootstrap();
    private static Map<String, Channel> channels = new ConcurrentHashMap<>();

    public static Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        if (channels.containsKey(key)) {
            // 判断是否有对应地址的连接
            Channel channel = channels.get(key);
            if (channels != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }
        // CountDownLatch 允许 count 个线程阻塞在一个地方，直至所有线程的任务都执行完毕。
        // 当调用 await() 方法的时候，如果 state 不为 0，那就证明任务还没有执行完毕，await() 方法就会一直阻塞.
        // 直到count 个线程调用了countDown()使 state值被减为 0，或者调用await()的线程被中断，该线程才会从阻塞中被唤醒，执行后面的语句。
        // CountDownLatch countDownLatch = new CountDownLatch(1);
        Channel channel = null;
        try {
            //connect(bootstrap, inetSocketAddress, countDownLatch);
            //countDownLatch.await();
            channel = connect(bootstrap, inetSocketAddress);
            channels.put(key, channel);
        } catch (InterruptedException | ExecutionException e) {
            log.error("occur exception when get channel:", e);
            throw new RuntimeException(e);
        }
        return channel;
    }

    /*
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, CountDownLatch countDownLatch) {
        connect(bootstrap, inetSocketAddress, countDownLatch, MAX_RETRY_COUNT);
    }*/

    /**
     * 带有重试机制的连接方法
     */
    private static Channel connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        // 创建一个 CompletableFuture 用于异步处理连接操作
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {

                log.info("connection successful");
                /*
                channel = future.channel();
                countDownLatch.countDown();
                return;
            }
            if (retry == 0) {
                log.error("connection failed: retry chance runs out, quit connection");
                countDownLatch.countDown();
                throw new RpcException(RpcError.CLIENT_CONNECT_SERVER_FAILURE);*/
                // 如果连接成功，将连接的 Channel 设置到 CompletableFuture，并标记操作完成
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
            /*
            int order = (MAX_RETRY_COUNT - retry) + 1; // 第几次重连
            int delay = 1 << order; // 本次重连的间隔，2的order次方
            log.error("{}: connection failed, no.{} reconnection", new Date(), order);
            bootstrap.config().group().schedule(() ->
                    connect(bootstrap, inetSocketAddress, countDownLatch, retry - 1), delay, TimeUnit.SECONDS);
                    */
        });
        return completableFuture.get();
    }
}
