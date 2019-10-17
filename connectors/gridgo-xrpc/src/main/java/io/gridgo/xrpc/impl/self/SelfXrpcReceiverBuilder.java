package io.gridgo.xrpc.impl.self;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.XrpcReceiver;
import lombok.NonNull;

public class SelfXrpcReceiverBuilder {

    private final @NonNull ConnectorResolver connectorResolver;

    private @NonNull String endpoint;

    public SelfXrpcReceiverBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public SelfXrpcReceiverBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public XrpcReceiver build() {
        var result = new SelfXrpcReceiver();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        return result;
    }
}
