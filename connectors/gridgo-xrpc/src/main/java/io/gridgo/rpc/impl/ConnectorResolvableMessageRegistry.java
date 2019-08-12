package io.gridgo.rpc.impl;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.rpc.ConnectorResolvable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class ConnectorResolvableMessageRegistry<TYPE_IN, TYPE_OUT>
        extends AbstractMessageRegistry<TYPE_IN, TYPE_OUT> implements ConnectorResolvable {

    @Setter
    @Getter
    private @NonNull ConnectorResolver connectorResolver;
}
