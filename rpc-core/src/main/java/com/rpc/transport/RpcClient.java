package com.rpc.transport;

import com.rpc.entity.RpcRequest;

/**
 * 客户端通用接口
 * 用于发送请求给服务端，对应socket和netty两种实现方式
 * */
public interface RpcClient {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
