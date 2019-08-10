package io.gridgo.rpc.impl;

import io.gridgo.connector.ConnectorResolver;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class ConnectorResolvableMessageRegistry<TYPE_IN, TYPE_OUT>
        extends AbstractMessageRegistry<TYPE_IN, TYPE_OUT> {

    @Setter
    @Getter
    private @NonNull ConnectorResolver connectorResolver;
}
