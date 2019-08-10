package io.gridgo.rpc.dynamic;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.rpc.RpcSender;
import lombok.NonNull;

public class DynamicRpcSenderBuilder {

    private final @NonNull ConnectorResolver connectorResolver;
    private String endpoint;
    private String replyEndpoint;
    private String replyTo;

    public DynamicRpcSenderBuilder(@NonNull ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public DynamicRpcSenderBuilder endpoint(@NonNull String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public DynamicRpcSenderBuilder replyEndpoint(@NonNull String replyEndpoint) {
        this.replyEndpoint = replyEndpoint;
        return this;
    }

    public DynamicRpcSenderBuilder replyTo(@NonNull String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public RpcSender build() {
        var result = new DynamicRpcSender();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setReplyEndpoint(replyEndpoint);
        result.setReplyTo(replyTo);
        return result;
    }
}
