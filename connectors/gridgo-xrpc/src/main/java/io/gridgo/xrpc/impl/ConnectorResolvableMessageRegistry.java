package io.gridgo.xrpc.impl;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.XrpcConnectorResolvable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class ConnectorResolvableMessageRegistry extends AbstractMessageRegistry
        implements XrpcConnectorResolvable {

    @Setter
    @Getter
    private @NonNull ConnectorResolver connectorResolver;
}
