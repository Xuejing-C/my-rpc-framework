package com.rpc.transport.socket.server;

import com.rpc.handler.RpcRequestHandler;
import com.rpc.registry.ServiceRegistry;
import com.rpc.transport.RpcServer;
import com.rpc.util.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Socket(BIO)服务端
 * */
@Slf4j
public class SocketRpcServer implements RpcServer {

    private final ExecutorService threadPool;
    private RpcRequestHandler rpcRequestHandler = new RpcRequestHandler();
    private final ServiceRegistry serviceRegistry;

    public SocketRpcServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
    }

    public void start (int port) {
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            log.info("server starts...");
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                log.info("client connected! IP address: {}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur exception when server starts:", e);
        }
    }
}
