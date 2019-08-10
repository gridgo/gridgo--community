package io.gridgo.rpc.fixed;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.rpc.RpcReceiver;
import lombok.NonNull;

public class FixedRpcReceiverBuilder {

    private final @NonNull ConnectorResolver connectorResolver;

    private @NonNull String endpoint;
    private @NonNull FixedRpcRequestUnpacker requestUnpacker = FixedRpcRequestUnpacker.DEFAULT;
    private @NonNull FixedRpcResponsePacker responsePacker = FixedRpcResponsePacker.DEFAULT;

    public FixedRpcReceiverBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public FixedRpcReceiverBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public FixedRpcReceiverBuilder requestUnpacker(FixedRpcRequestUnpacker requestUnpacker) {
        this.requestUnpacker = requestUnpacker;
        return this;
    }

    public FixedRpcReceiverBuilder responsePacker(FixedRpcResponsePacker responsePacker) {
        this.responsePacker = responsePacker;
        return this;
    }

    public RpcReceiver build() {
        var result = new FixedRpcReceiver();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setRequestUnpacker(requestUnpacker);
        result.setResponsePacker(responsePacker);
        return result;
    }
}
