package io.gridgo.xrpc.impl.fixed;

import io.gridgo.connector.ConnectorResolver;
import lombok.NonNull;

public class FixedXrpcSenderBuilder {

    @NonNull
    private final ConnectorResolver connectorResolver;

    @NonNull
    private String endpoint;

    public FixedXrpcSenderBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public FixedXrpcSender build() {
        FixedXrpcSender result = new FixedXrpcSender();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        return result;
    }
}
