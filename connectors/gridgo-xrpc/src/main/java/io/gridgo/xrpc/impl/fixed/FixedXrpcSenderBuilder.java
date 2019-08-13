package io.gridgo.xrpc.impl.fixed;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.gridgo.bean.BValue;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.decorator.XrpcMessageDecorator;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.decorator.XrpcResponseDecorator;
import io.gridgo.xrpc.decorator.corrid.CorrIdSenderCodec;
import io.gridgo.xrpc.registry.XrpcSenderRegistry;
import lombok.NonNull;

public class FixedXrpcSenderBuilder {

    private static final AtomicLong DEFAULT_CORR_ID_SEED = new AtomicLong(0);

    @NonNull
    private final ConnectorResolver connectorResolver;

    @NonNull
    private String endpoint;

    @NonNull
    private String corrIdFieldName = "gridgo-xrpc-corr-id";

    @NonNull
    private Supplier<BValue> idGenerator = () -> BValue.of(DEFAULT_CORR_ID_SEED.getAndIncrement());

    public FixedXrpcSenderBuilder(ConnectorResolver connectorResolver) {
        this.connectorResolver = connectorResolver;
    }

    public FixedXrpcSenderBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public FixedXrpcSenderBuilder corrIdFieldName(String corrIdFieldName) {
        this.corrIdFieldName = corrIdFieldName;
        return this;
    }

    public FixedXrpcSenderBuilder idGenerator(Supplier<BValue> idGenerator) {
        this.idGenerator = idGenerator;
        return this;
    }

    private XrpcSenderRegistry buildMessageRegistry() {
        var decorators = new LinkedList<XrpcMessageDecorator>();

        decorators.add(0, CorrIdSenderCodec.builder() //
                .deferredCache(new NonBlockingHashMap<>()) //
                .fieldName(corrIdFieldName) //
                .idGenerator(idGenerator) //
                .build());

        var builder = XrpcSenderRegistry.builder();
        XrpcSenderRegistry result = builder.build();
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

    public FixedXrpcSender build() {
        FixedXrpcSender result = new FixedXrpcSender();
        result.setConnectorResolver(connectorResolver);
        result.setEndpoint(endpoint);
        result.setMessageRegistry(buildMessageRegistry());
        return result;
    }
}
