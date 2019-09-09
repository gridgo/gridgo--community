package io.gridgo.xrpc.impl.fixed;

import java.util.LinkedList;
import java.util.function.Function;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.decorator.XrpcAckResponder;
import io.gridgo.xrpc.decorator.XrpcMessageDecorator;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.decorator.XrpcResponseDecorator;
import io.gridgo.xrpc.decorator.corrid.CorrIdReceiverCodec;
import io.gridgo.xrpc.registry.XrpcReceiverRegistry;
import io.gridgo.xrpc.registry.impl.DefaultReceiverRegistry;
import lombok.NonNull;

public class FixedXrpcReceiverBuilder {

    @NonNull
    private final ConnectorResolver connectorResolver;

    @NonNull
    private String endpoint;

    @NonNull
    private String corrIdFieldName = "gridgo-xrpc-corr-id";

    @NonNull
    private Function<Exception, Message> failureHandler = (ex) -> Message.ofAny("failed: " + ex.getMessage());

    @NonNull
    private XrpcAckResponder ackResponder = XrpcAckResponder.DEFAULT;

    public FixedXrpcReceiverBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public FixedXrpcReceiverBuilder corrIdFieldName(String corrIdFieldName) {
        this.corrIdFieldName = corrIdFieldName;
        return this;
    }

    private XrpcReceiverRegistry buildRequestRegistry() {
        var decorators = new LinkedList<XrpcMessageDecorator>();

        decorators.add(0, new CorrIdReceiverCodec(corrIdFieldName));

        var result = new DefaultReceiverRegistry();
        result.setFailureHandler(failureHandler);
        decorators.forEach(decorator -> {
            if (decorator instanceof XrpcRequestDecorator) {
                result.getRequestDecorators().add((XrpcRequestDecorator) decorator);
            }
            if (decorator instanceof XrpcResponseDecorator) {
                result.getResponseDecorators().add((XrpcResponseDecorator) decorator);
            }
        });
        return result;
    }

    public FixedXrpcReceiverBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public FixedXrpcReceiver build() {
        FixedXrpcReceiver result = new FixedXrpcReceiver();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setRequestRegistry(buildRequestRegistry());
        return result;
    }
}
