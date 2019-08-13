package io.gridgo.xrpc.impl.fixed;

import io.gridgo.connector.ConnectorResolver;
import lombok.NonNull;

public class FixedXrpcReceiverBuilder {

    @NonNull
    private final ConnectorResolver connectorResolver;

    @NonNull
    private String endpoint;

    public FixedXrpcReceiverBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public FixedXrpcReceiver build() {
        FixedXrpcReceiver result = new FixedXrpcReceiver();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        return result;
    }
}
