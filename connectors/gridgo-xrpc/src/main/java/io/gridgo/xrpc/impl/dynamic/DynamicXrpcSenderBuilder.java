package io.gridgo.xrpc.impl.dynamic;

import java.util.LinkedList;
import java.util.List;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.decorator.XrpcMessageDecorator;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.decorator.XrpcResponseDecorator;
import io.gridgo.xrpc.decorator.replyto.ReplyToSenderDecorator;
import io.gridgo.xrpc.registry.XrpcSenderRegistry;
import lombok.NonNull;

public class DynamicXrpcSenderBuilder {

    @NonNull
    private final ConnectorResolver connectorResolver;

    @NonNull
    private String endpoint;

    @NonNull
    private String replyTo;

    @NonNull
    private String replyEndpoint;

    @NonNull
    private final List<XrpcMessageDecorator> decorators = new LinkedList<>();

    @NonNull
    private String replyToFieldName = "gridgo-xrpc-reply-to";

    public DynamicXrpcSenderBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    private XrpcSenderRegistry buildRegistry() {
        this.decorators.add(0, ReplyToSenderDecorator.builder() //
                .replyTo(replyTo) //
                .fieldName(replyToFieldName) //
                .build());

        var builder = XrpcSenderRegistry.builder();
        decorators.forEach(decorator -> {
            if (decorator instanceof XrpcRequestDecorator) {
                builder.requestDecorator((XrpcRequestDecorator) decorator);
            }
            if (decorator instanceof XrpcResponseDecorator) {
                builder.responseDecorator((XrpcResponseDecorator) decorator);
            }
        });
        return builder.build();
    }

    public DynamicXrpcSender build() {
        DynamicXrpcSender result = new DynamicXrpcSender();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setReplyEndpoint(replyEndpoint);
        result.setMessageRegistry(buildRegistry());
        return result;
    }
}
