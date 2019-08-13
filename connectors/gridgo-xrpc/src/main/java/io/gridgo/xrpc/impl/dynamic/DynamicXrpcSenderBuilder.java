package io.gridgo.xrpc.impl.dynamic;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.gridgo.bean.BValue;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.decorator.XrpcMessageDecorator;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.decorator.XrpcResponseDecorator;
import io.gridgo.xrpc.decorator.corrid.CorrIdSenderCodec;
import io.gridgo.xrpc.decorator.corrid.CorrIdTypeConverterSenderCodec;
import io.gridgo.xrpc.decorator.replyto.ReplyToSenderDecorator;
import io.gridgo.xrpc.registry.XrpcSenderRegistry;
import io.gridgo.xrpc.registry.impl.DefaultSenderRegistry;
import lombok.NonNull;

public class DynamicXrpcSenderBuilder {

    private static final AtomicLong DEFAULT_CORR_ID_SEED = new AtomicLong(0);

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

    @NonNull
    private Supplier<BValue> idGenerator = () -> BValue.of(DEFAULT_CORR_ID_SEED.getAndIncrement());

    public DynamicXrpcSenderBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public DynamicXrpcSenderBuilder corrIdFieldName(String corrIdFieldName) {
        this.corrIdFieldName = corrIdFieldName;
        return this;
    }

    public DynamicXrpcSenderBuilder idGenerator(Supplier<BValue> idGenerator) {
        this.idGenerator = idGenerator;
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

    private XrpcSenderRegistry buildMessageRegistry() {
        this.decorators.add(0, ReplyToSenderDecorator.builder() //
                .replyTo(replyTo) //
                .fieldName(replyToFieldName) //
                .build());

        var convertCorrIdToLong = CorrIdTypeConverterSenderCodec.builder() //
                .fieldName(corrIdFieldName) //
                .targetType(Long.class) //
                .build();

        this.decorators.add(convertCorrIdToLong.getRequestDecorator());
        this.decorators.add(0, convertCorrIdToLong.getResponseDecorator());

        this.decorators.add(0, CorrIdSenderCodec.builder() //
                .deferredCache(new NonBlockingHashMap<>()) //
                .fieldName(corrIdFieldName) //
                .idGenerator(idGenerator) //
                .build());

        var builder = XrpcSenderRegistry.builder();
        DefaultSenderRegistry result = builder.build();
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
