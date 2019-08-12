package io.gridgo.rpc;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.rpc.impl.dynamic.DynamicRpcReceiverBuilder;
import io.gridgo.rpc.impl.dynamic.DynamicRpcSenderBuilder;
import io.gridgo.rpc.impl.self.SelfRpcReceiverBuilder;
import io.gridgo.rpc.impl.self.SelfRpcSenderBuilder;
import lombok.NonNull;

public class RpcBuilder {

    public static RpcBuilder newBuilder(ConnectorResolver connectorResolver) {
        return new RpcBuilder(connectorResolver);
    }

    private final @NonNull ConnectorResolver connectorResolver;

    private RpcBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public DynamicRpcSenderBuilder dynamicSender() {
        return new DynamicRpcSenderBuilder(connectorResolver);
    }

    public DynamicRpcReceiverBuilder dynamicReceiver() {
        return new DynamicRpcReceiverBuilder(connectorResolver);
    }

    public SelfRpcReceiverBuilder selfReceiver() {
        return new SelfRpcReceiverBuilder(connectorResolver);
    }

    public SelfRpcSenderBuilder selfSender() {
        return new SelfRpcSenderBuilder(connectorResolver);
    }
}
