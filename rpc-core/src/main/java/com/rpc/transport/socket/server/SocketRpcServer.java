package com.rpc.transport.socket.server;

import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

@Slf4j
public class SocketRpcServer {
    private final ExecutorService threadPool;

    public SocketRpcServer() {
        int corePoolSize = 5;
        int maximumPoolSize = 50;
        long keepAliveTime = 60;
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workingQueue, threadFactory);
    }

    /**
     * 服务端注册服务
     * @param service 被代理对象(真实对象)
     * */
    public void register (Object service, int port) {
        if (null == service) {
            throw new RpcException(RpcError.SERVICE_CAN_NOT_BE_NULL);
        }
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            log.info("server starts...");
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                log.info("client connected! IP address: " + socket.getInetAddress() + ":" + socket.getPort());
                threadPool.execute(new RequestHandler(socket, service));
            }
        } catch (IOException e) {
            log.error("Occur exception:", e);
        }
    }
}
