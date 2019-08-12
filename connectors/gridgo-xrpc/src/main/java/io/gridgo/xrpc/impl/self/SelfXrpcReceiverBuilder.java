package io.gridgo.xrpc.impl.self;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.XrpcReceiver;
import lombok.NonNull;

public class SelfXrpcReceiverBuilder {

    private final @NonNull ConnectorResolver connectorResolver;

    private @NonNull String endpoint;
    private @NonNull SelfXrpcRequestUnpacker requestUnpacker = SelfXrpcRequestUnpacker.BODY;
    private @NonNull SelfXrpcResponsePacker responsePacker = SelfXrpcResponsePacker.BODY;

    public SelfXrpcReceiverBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public SelfXrpcReceiverBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public SelfXrpcReceiverBuilder requestUnpacker(SelfXrpcRequestUnpacker requestUnpacker) {
        this.requestUnpacker = requestUnpacker;
        return this;
    }

    public SelfXrpcReceiverBuilder responsePacker(SelfXrpcResponsePacker responsePacker) {
        this.responsePacker = responsePacker;
        return this;
    }

    public XrpcReceiver build() {
        var result = new SelfXrpcReceiver();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setRequestUnpacker(requestUnpacker);
        result.setResponsePacker(responsePacker);
        return result;
    }
}
