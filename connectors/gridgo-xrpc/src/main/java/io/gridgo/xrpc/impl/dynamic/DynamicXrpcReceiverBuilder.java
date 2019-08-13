package io.gridgo.xrpc.impl.dynamic;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.decorator.XrpcAckResponder;
import io.gridgo.xrpc.decorator.XrpcMessageDecorator;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.decorator.XrpcResponseDecorator;
import io.gridgo.xrpc.decorator.corrid.CorrIdReceiverCodec;
import io.gridgo.xrpc.decorator.corrid.CorrIdTypeConverterSenderCodec;
import io.gridgo.xrpc.decorator.replyto.ReplyToReceiverDecorator;
import io.gridgo.xrpc.registry.XrpcReceiverRegistry;
import io.gridgo.xrpc.responder.XrpcResponderLookupable;
import io.gridgo.xrpc.responder.impl.MultiConnectorResponderLookupable;
import lombok.NonNull;

public class DynamicXrpcReceiverBuilder {

    @NonNull
    private final ConnectorResolver connectorResolver;

    @NonNull
    private String endpoint;

    @NonNull
    private final List<XrpcMessageDecorator> decorators = new LinkedList<>();

    @NonNull
    private String replyToFieldName = "gridgo-xrpc-reply-to";

    @NonNull
    private String corrIdFieldName = "gridgo-xrpc-corr-id";

    @NonNull
    private Function<Exception, Message> failureHandler = (ex) -> Message.ofAny("failed: " + ex.getMessage());

    @NonNull
    private XrpcResponderLookupable responderRegistry;

    @NonNull
    private XrpcAckResponder ackResponder = XrpcAckResponder.DEFAULT;

    public DynamicXrpcReceiverBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public DynamicXrpcReceiverBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public DynamicXrpcReceiverBuilder replyToFieldName(String replyToFieldName) {
        this.replyToFieldName = replyToFieldName;
        return this;
    }

    public DynamicXrpcReceiverBuilder corrIdFieldName(String corrIdFieldName) {
        this.corrIdFieldName = corrIdFieldName;
        return this;
    }

    public DynamicXrpcReceiverBuilder failureHandler(Function<Exception, Message> failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

    public DynamicXrpcReceiverBuilder responderRegistry(XrpcResponderLookupable responderRegistry) {
        this.responderRegistry = responderRegistry;
        return this;
    }

    private XrpcReceiverRegistry buildRegistry() {

        this.decorators.add(0, ReplyToReceiverDecorator.builder() //
                .fieldName(replyToFieldName) //
                .build());

        this.decorators.add(0, CorrIdReceiverCodec.builder() //
                .fieldName(corrIdFieldName) //
                .build());

        var convertCorrIdToLong = CorrIdTypeConverterSenderCodec.builder() //
                .fieldName(corrIdFieldName) //
                .targetType(Long.class) //
                .build();

        this.decorators.add(0, convertCorrIdToLong.getRequestDecorator());
        this.decorators.add(convertCorrIdToLong.getResponseDecorator());

        var builder = XrpcReceiverRegistry.builder();

        builder.failureHandler(failureHandler);
        XrpcReceiverRegistry result = builder.build();

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

    public DynamicXrpcReceiver build() {
        DynamicXrpcReceiver result = new DynamicXrpcReceiver();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setMessageRegistry(buildRegistry());
        result.setAckResponder(ackResponder);

        if (responderRegistry == null) {
            responderRegistry = new MultiConnectorResponderLookupable(connectorResolver);
        }
        result.setResponderRegistry(responderRegistry);

        return result;
    }
}
