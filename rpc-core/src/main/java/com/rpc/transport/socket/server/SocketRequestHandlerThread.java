package com.rpc.transport.socket.server;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.handler.RpcRequestHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 处理RpcRequest的工作线程
 * */
@Slf4j
@AllArgsConstructor
public class SocketRequestHandlerThread implements Runnable{
    private Socket socket;
    private RpcRequestHandler rpcRequestHandler;

    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();

            Object result = rpcRequestHandler.handle(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (ClassNotFoundException | IOException e) {
            log.error("occur exception:", e);
        }
    }
}
