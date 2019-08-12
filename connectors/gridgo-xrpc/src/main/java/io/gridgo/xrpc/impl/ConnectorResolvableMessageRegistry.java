package io.gridgo.xrpc.impl;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.ConnectorResolvable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class ConnectorResolvableMessageRegistry<TYPE_IN, TYPE_OUT>
        extends AbstractMessageRegistry<TYPE_IN, TYPE_OUT> implements ConnectorResolvable {

    @Setter
    @Getter
    private @NonNull ConnectorResolver connectorResolver;
}
