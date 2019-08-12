package io.gridgo.xrpc.impl.dynamic;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.XrpcSender;
import lombok.NonNull;

public class DynamicXrpcSenderBuilder {

    private final @NonNull ConnectorResolver connectorResolver;
    private String endpoint;
    private String replyEndpoint;
    private String replyTo;

    public DynamicXrpcSenderBuilder(@NonNull ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public DynamicXrpcSenderBuilder endpoint(@NonNull String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public DynamicXrpcSenderBuilder replyEndpoint(@NonNull String replyEndpoint) {
        this.replyEndpoint = replyEndpoint;
        return this;
    }

    public DynamicXrpcSenderBuilder replyTo(@NonNull String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public XrpcSender build() {
        var result = new DynamicXrpcSender();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setReplyEndpoint(replyEndpoint);
        result.setReplyTo(replyTo);
        return result;
    }
}
