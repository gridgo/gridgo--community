package io.gridgo.rpc.dynamic;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.rpc.RpcReceiver;
import lombok.NonNull;

public class DynamicRpcReceiverBuilder {

    private final @NonNull ConnectorResolver connectorResolver;

    private @NonNull String endpoint;
    private @NonNull DynamicRpcResponder<?> responder = new CorrIdRpcResponder();
    private @NonNull DynamicRpcAckResponder ackResponder = DynamicRpcAckResponder.DEFAULT;

    public DynamicRpcReceiverBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public DynamicRpcReceiverBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public DynamicRpcReceiverBuilder responder(DynamicRpcResponder<?> responder) {
        this.responder = responder;
        return this;
    }

    public DynamicRpcReceiverBuilder ackResponder(DynamicRpcAckResponder ackResponder) {
        this.ackResponder = ackResponder;
        return this;
    }

    public RpcReceiver build() {
        var result = new DynamicRpcReceiver();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setResponder(responder);
        result.setAckResponder(ackResponder);
        return result;
    }
}
