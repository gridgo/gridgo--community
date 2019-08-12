package io.gridgo.xrpc.impl.dynamic;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.XrpcReceiver;
import lombok.NonNull;

public class DynamicXrpcReceiverBuilder {

    private final @NonNull ConnectorResolver connectorResolver;

    private @NonNull String endpoint;
    private @NonNull DynamicXrpcResponder<?> responder = new CorrIdXrpcResponder();
    private @NonNull DynamicXrpcAckResponder ackResponder = DynamicXrpcAckResponder.DEFAULT;

    public DynamicXrpcReceiverBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public DynamicXrpcReceiverBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public DynamicXrpcReceiverBuilder responder(DynamicXrpcResponder<?> responder) {
        this.responder = responder;
        return this;
    }

    public DynamicXrpcReceiverBuilder ackResponder(DynamicXrpcAckResponder ackResponder) {
        this.ackResponder = ackResponder;
        return this;
    }

    public XrpcReceiver build() {
        var result = new DynamicXrpcReceiver();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setResponder(responder);
        result.setAckResponder(ackResponder);
        return result;
    }
}
