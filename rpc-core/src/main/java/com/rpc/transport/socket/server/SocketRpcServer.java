package com.rpc.transport.socket.server;

import com.rpc.handler.RpcRequestHandler;
import com.rpc.provider.ServiceProvider;
import com.rpc.provider.ServiceProviderImpl;
import com.rpc.registry.NacosServiceRegistry;
import com.rpc.registry.ServiceRegistry;
import com.rpc.transport.RpcServer;
import com.rpc.util.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Socket(BIO)服务端
 * */
@Slf4j
public class SocketRpcServer implements RpcServer {
    private final String host;
    private final int port;
    private final ExecutorService threadPool;
    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;
    private RpcRequestHandler rpcRequestHandler = new RpcRequestHandler();

    public SocketRpcServer(String host, int port) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
    }

    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }

    @Override
    public void start () {
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
