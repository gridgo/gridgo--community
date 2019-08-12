package io.gridgo.rpc.impl.self;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.rpc.RpcReceiver;
import lombok.NonNull;

public class SelfRpcReceiverBuilder {

    private final @NonNull ConnectorResolver connectorResolver;

    private @NonNull String endpoint;
    private @NonNull SelfRpcRequestUnpacker requestUnpacker = SelfRpcRequestUnpacker.BODY;
    private @NonNull SelfRpcResponsePacker responsePacker = SelfRpcResponsePacker.BODY;

    public SelfRpcReceiverBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public SelfRpcReceiverBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public SelfRpcReceiverBuilder requestUnpacker(SelfRpcRequestUnpacker requestUnpacker) {
        this.requestUnpacker = requestUnpacker;
        return this;
    }

    public SelfRpcReceiverBuilder responsePacker(SelfRpcResponsePacker responsePacker) {
        this.responsePacker = responsePacker;
        return this;
    }

    public RpcReceiver build() {
        var result = new SelfRpcReceiver();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setRequestUnpacker(requestUnpacker);
        result.setResponsePacker(responsePacker);
        return result;
    }
}
