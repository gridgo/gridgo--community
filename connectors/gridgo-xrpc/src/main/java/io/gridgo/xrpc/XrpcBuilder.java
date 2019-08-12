package io.gridgo.xrpc;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.impl.dynamic.DynamicXrpcReceiverBuilder;
import io.gridgo.xrpc.impl.dynamic.DynamicXrpcSenderBuilder;
import io.gridgo.xrpc.impl.self.SelfXrpcReceiverBuilder;
import io.gridgo.xrpc.impl.self.SelfXrpcSenderBuilder;
import lombok.NonNull;

public class XrpcBuilder {

    public static XrpcBuilder newBuilder(ConnectorResolver connectorResolver) {
        return new XrpcBuilder(connectorResolver);
    }

    private final @NonNull ConnectorResolver connectorResolver;

    private XrpcBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public DynamicXrpcSenderBuilder dynamicSender() {
        return new DynamicXrpcSenderBuilder(connectorResolver);
    }

    public DynamicXrpcReceiverBuilder dynamicReceiver() {
        return new DynamicXrpcReceiverBuilder(connectorResolver);
    }

    public SelfXrpcReceiverBuilder selfReceiver() {
        return new SelfXrpcReceiverBuilder(connectorResolver);
    }

    public SelfXrpcSenderBuilder selfSender() {
        return new SelfXrpcSenderBuilder(connectorResolver);
    }
}
