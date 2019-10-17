package io.gridgo.xrpc.impl.dynamic;

import java.util.LinkedList;
import java.util.List;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.decorator.XrpcMessageDecorator;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.decorator.XrpcResponseDecorator;
import io.gridgo.xrpc.decorator.corrid.CorrIdHexSenderCodec;
import io.gridgo.xrpc.decorator.corrid.CorrIdSenderCodec;
import io.gridgo.xrpc.decorator.replyto.ReplyToSenderDecorator;
import io.gridgo.xrpc.registry.XrpcSenderRegistry;
import io.gridgo.xrpc.registry.impl.DefaultSenderRegistry;
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

    @NonNull
    private String corrIdFieldName = "gridgo-xrpc-corr-id";

    private boolean encodeCorrIdToHex = false;

    private boolean decodeCorrIdFromHex = false;

    public DynamicXrpcSenderBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public DynamicXrpcSenderBuilder corrIdFieldName(String corrIdFieldName) {
        this.corrIdFieldName = corrIdFieldName;
        return this;
    }

    public DynamicXrpcSenderBuilder clearDecorators() {
        this.decorators.clear();
        return this;
    }

    public DynamicXrpcSenderBuilder addDecorator(@NonNull XrpcMessageDecorator decorator) {
        this.decorators.add(decorator);
        return this;
    }

    public DynamicXrpcSenderBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public DynamicXrpcSenderBuilder replyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public DynamicXrpcSenderBuilder replyEndpoint(String replyEndpoint) {
        this.replyEndpoint = replyEndpoint;
        return this;
    }

    public DynamicXrpcSenderBuilder replyToFieldName(String replyToFieldName) {
        this.replyToFieldName = replyToFieldName;
        return this;
    }

    public DynamicXrpcSenderBuilder encodeCorrIdAsHex() {
        this.encodeCorrIdToHex = true;
        return this;
    }

    public DynamicXrpcSenderBuilder encodeCorrIdToHex(boolean value) {
        this.encodeCorrIdToHex = value;
        return this;
    }

    public DynamicXrpcSenderBuilder decodeCorrIdFromHex() {
        this.decodeCorrIdFromHex = true;
        return this;
    }

    public DynamicXrpcSenderBuilder decodeCorrIdFromHex(boolean value) {
        this.decodeCorrIdFromHex = value;
        return this;
    }

    private XrpcSenderRegistry buildMessageRegistry() {
        var hexCodec = new CorrIdHexSenderCodec(corrIdFieldName);
        var corrIdInjector = new CorrIdSenderCodec(corrIdFieldName);

        decorators.add(corrIdInjector.getRequestDecorator());
        if (encodeCorrIdToHex)
            decorators.add(hexCodec.getRequestDecorator());

        if (decodeCorrIdFromHex)
            decorators.add(hexCodec.getResponseDecorator());
        decorators.add(corrIdInjector.getResponseDecorator());

        decorators.add(new ReplyToSenderDecorator(replyToFieldName, replyTo));

        var result = new DefaultSenderRegistry();
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

    public DynamicXrpcSender build() {
        DynamicXrpcSender result = new DynamicXrpcSender();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setReplyEndpoint(replyEndpoint);
        result.setMessageRegistry(buildMessageRegistry());
        return result;
    }

}
