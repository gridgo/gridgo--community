package io.gridgo.xrpc.impl.self;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.XrpcSender;
import lombok.NonNull;

public class SelfXrpcSenderBuilder {

    private final @NonNull ConnectorResolver connectorResolver;

    private @NonNull String endpoint;
    private @NonNull SelfXrpcRequestPacker requestPacker = SelfXrpcRequestPacker.BODY;
    private @NonNull SelfXrpcResponseUnpacker responseUnpacker = SelfXrpcResponseUnpacker.BODY;

    public SelfXrpcSenderBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public SelfXrpcSenderBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public SelfXrpcSenderBuilder requestPacker(SelfXrpcRequestPacker requestPacker) {
        this.requestPacker = requestPacker;
        return this;
    }

    public SelfXrpcSenderBuilder responseUnpacker(SelfXrpcResponseUnpacker responseUnpacker) {
        this.responseUnpacker = responseUnpacker;
        return this;
    }

    public XrpcSender build() {
        var result = new SelfXrpcSender();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setRequestPacker(requestPacker);
        result.setResponseUnpacker(responseUnpacker);
        return result;
    }
}
