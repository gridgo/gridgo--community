package io.gridgo.xrpc.impl.self;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.XrpcSender;
import lombok.NonNull;

public class SelfXrpcSenderBuilder {

    private final @NonNull ConnectorResolver connectorResolver;

    private @NonNull String endpoint;

    public SelfXrpcSenderBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public SelfXrpcSenderBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public XrpcSender build() {
        var result = new SelfXrpcSender();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        return result;
    }
}
