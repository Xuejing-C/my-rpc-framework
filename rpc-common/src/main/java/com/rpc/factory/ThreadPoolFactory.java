package com.rpc.factory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 线程池工具类
 * */
@NoArgsConstructor
@Slf4j
public class ThreadPoolFactory {
    /**
     * 线程池参数
     */
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAXIMUM_POOL_SIZE_SIZE = 100;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;
    private static Map<String, ExecutorService> threadPollsMap = new ConcurrentHashMap<>();

    /**
     * 创建带有自定义线程名前缀的线程池
     */
    public static ExecutorService createDefaultThreadPool(String threadNamePrefix) {
        return createDefaultThreadPool(threadNamePrefix, false);
    }

    public static ExecutorService createDefaultThreadPool(String threadNamePrefix, Boolean daemon) {
        ExecutorService pool = threadPollsMap.computeIfAbsent(threadNamePrefix, k -> createThreadPool(threadNamePrefix, daemon));
        if (pool.isShutdown() || pool.isTerminated()) {
            threadPollsMap.remove(threadNamePrefix);
            pool = createThreadPool(threadNamePrefix, daemon);
            threadPollsMap.put(threadNamePrefix, pool);
        }
        return pool;
    }

    /**
     * 关闭所有线程池(服务端使用线程池来处理rpcRequest)
     */
    public static void shutDownAll() {
        log.info("close all thread pools");
        // 使用 parallelStream() 并行关闭所有线程池，提高关闭效率
        threadPollsMap.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("shut down thread pool [{}] [{}]", entry.getKey(), entry.getValue());
            try {
                // 等待线程池终止，最多等待10秒
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("thread pool shutdown failure");
                executorService.shutdownNow(); // 如果等待超时，强制关闭线程池
            }
        });
    }

    /**
     * 创建带有自定义线程名前缀的线程池。
     * @param threadNamePrefix 线程名前缀
     * @param daemon 是否为守护线程
     * @return 自定义线程池
     */
    public static ExecutorService createThreadPool(String threadNamePrefix, Boolean daemon) {
        // 使用有界队列
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE_SIZE, KEEP_ALIVE_TIME, TimeUnit.MINUTES, workQueue, threadFactory);
    }

    /**
     * 创建 ThreadFactory
     * 如果threadNamePrefix不为空则使用自建ThreadFactory，否则使用defaultThreadFactory
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param daemon           指定是否为 Daemon Thread(守护线程)
     * @return ThreadFactory
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }
}
