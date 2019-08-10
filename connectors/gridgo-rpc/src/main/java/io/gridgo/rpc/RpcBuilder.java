package io.gridgo.rpc;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.rpc.dynamic.DynamicRpcReceiverBuilder;
import io.gridgo.rpc.dynamic.DynamicRpcSenderBuilder;
import io.gridgo.rpc.fixed.FixedRpcReceiverBuilder;
import io.gridgo.rpc.fixed.FixedRpcSenderBuilder;
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

    public FixedRpcReceiverBuilder fixedReceiver() {
        return new FixedRpcReceiverBuilder(connectorResolver);
    }

    public FixedRpcSenderBuilder fixedSender() {
        return new FixedRpcSenderBuilder(connectorResolver);
    }
}
