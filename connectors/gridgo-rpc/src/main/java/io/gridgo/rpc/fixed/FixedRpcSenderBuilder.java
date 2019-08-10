package io.gridgo.rpc.fixed;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.rpc.RpcSender;
import lombok.NonNull;

public class FixedRpcSenderBuilder {

    private final @NonNull ConnectorResolver connectorResolver;

    private @NonNull String endpoint;
    private @NonNull FixedRpcRequestPacker requestPacker = FixedRpcRequestPacker.DEFAULT;
    private @NonNull FixedRpcResponseUnpacker responseUnpacker = FixedRpcResponseUnpacker.DEFAULT;

    public FixedRpcSenderBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public FixedRpcSenderBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public FixedRpcSenderBuilder requestPacker(FixedRpcRequestPacker requestPacker) {
        this.requestPacker = requestPacker;
        return this;
    }

    public FixedRpcSenderBuilder responseUnpacker(FixedRpcResponseUnpacker responseUnpacker) {
        this.responseUnpacker = responseUnpacker;
        return this;
    }

    public RpcSender build() {
        var result = new FixedRpcSender();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setRequestPacker(requestPacker);
        result.setResponseUnpacker(responseUnpacker);
        return result;
    }
}
