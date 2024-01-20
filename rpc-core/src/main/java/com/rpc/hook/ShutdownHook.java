package com.rpc.hook;

import com.rpc.factory.ThreadPoolFactory;
import com.rpc.util.NacosUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * Shutdown Hook 是 Java 虚拟机提供的一种机制，允许在程序关闭时执行一些清理或资源释放的操作。
 * 当服务端（provider）关闭时，清除其所有服务实例。
 */
@Slf4j
public class ShutdownHook {
    private static final ShutdownHook shutdownHook = new ShutdownHook();

    /**
     * 获取 ShutdownHook 实例
     * */
    public static ShutdownHook getShutdownHook() {
        return shutdownHook;
    }

    /**
     * 添加 Shutdown Hook
     * */
    public void addClearAllHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            NacosUtil.clearService(); // 清除服务实例
            ThreadPoolFactory.shutDownAll(); // 关闭所有线程池
        }));
    }
}