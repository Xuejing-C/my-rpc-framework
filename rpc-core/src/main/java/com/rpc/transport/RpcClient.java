package com.rpc.transport;

import com.rpc.entity.RpcRequest;

/**
 * 客户端通用接口
 * */
public interface RpcClient {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
