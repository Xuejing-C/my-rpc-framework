package com.rpc.transport.socket.server;

import com.rpc.handler.RpcRequestHandler;
import com.rpc.hook.ShutdownHook;
import com.rpc.provider.ServiceProviderImpl;
import com.rpc.registry.NacosServiceRegistry;
import com.rpc.transport.AbstractRpcServer;
import com.rpc.factory.ThreadPoolFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Socket(BIO)服务端
 * */
public class SocketRpcServer extends AbstractRpcServer {
    //private final String host;
    //private final int port;
    private final ExecutorService threadPool;
    //private final ServiceRegistry serviceRegistry;
    // private final ServiceProvider serviceProvider;
    private RpcRequestHandler rpcRequestHandler = new RpcRequestHandler();

    public SocketRpcServer(String host, int port) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
        scanServices();
    }

    /*
    @Override
    public <T> void publishService(T service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service, serviceClass);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }*/

    @Override
    public void start () {
        try (ServerSocket serverSocket = new ServerSocket();) {
            serverSocket.bind(new InetSocketAddress(host, port));
            logger.info("server starts...");
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("client connected! IP address: {}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(socket, rpcRequestHandler));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("occur exception when server starts:", e);
        }
    }
}
