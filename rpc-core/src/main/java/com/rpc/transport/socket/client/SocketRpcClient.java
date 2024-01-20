package com.rpc.transport.socket.client;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.enumeration.ResponseCode;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.registry.NacosServiceDiscovery;
import com.rpc.registry.ServiceDiscovery;
import com.rpc.transport.RpcClient;
import com.rpc.util.RpcMessageChecker;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Socket(BIO)客户端
 * */
@Slf4j
public class SocketRpcClient implements RpcClient {
    private final ServiceDiscovery serviceDiscovery;
    public SocketRpcClient() {
        this.serviceDiscovery = new NacosServiceDiscovery();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();
            if (rpcResponse == null) {
                log.error("service invocation failure, service: {}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if (rpcResponse.getCode() == null || rpcResponse.getCode() != ResponseCode.SUCCESS.getCode()) {
                log.error("service invocation failure, service: {}, response: {}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service" + rpcRequest.getInterfaceName());
            }
            RpcMessageChecker.check(rpcResponse, rpcRequest);
            return rpcResponse;
        } catch (IOException | ClassNotFoundException e) {
            log.error("Occur exception:", e);
            throw new RpcException("service invocation failure: ", e);
        }
    }
}
