package io.gridgo.rpc.impl.self;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.rpc.RpcSender;
import lombok.NonNull;

public class SelfRpcSenderBuilder {

    private final @NonNull ConnectorResolver connectorResolver;

    private @NonNull String endpoint;
    private @NonNull SelfRpcRequestPacker requestPacker = SelfRpcRequestPacker.BODY;
    private @NonNull SelfRpcResponseUnpacker responseUnpacker = SelfRpcResponseUnpacker.BODY;

    public SelfRpcSenderBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public SelfRpcSenderBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public SelfRpcSenderBuilder requestPacker(SelfRpcRequestPacker requestPacker) {
        this.requestPacker = requestPacker;
        return this;
    }

    public SelfRpcSenderBuilder responseUnpacker(SelfRpcResponseUnpacker responseUnpacker) {
        this.responseUnpacker = responseUnpacker;
        return this;
    }

    public RpcSender build() {
        var result = new SelfRpcSender();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setRequestPacker(requestPacker);
        result.setResponseUnpacker(responseUnpacker);
        return result;
    }
}
