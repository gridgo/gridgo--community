package io.gridgo.xrpc.impl;

import java.util.Map;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.framework.impl.NonameComponentLifecycle;
import io.gridgo.xrpc.XrpcConnectorResolvable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractConnectorResolvable extends NonameComponentLifecycle implements XrpcConnectorResolvable {

    private final Map<String, Connector> connectorCache = new NonBlockingHashMap<>();

    @Setter
    @Getter
    private @NonNull ConnectorResolver connectorResolver;

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {
        
    }

    @Override
    public Connector resolveConnector(String url) {
        return connectorCache.compute(url, (key, connector) -> {
            if (connector == null || !connector.isStarted())
                return XrpcConnectorResolvable.super.resolveConnector(key);
            return connector;
        });
    }
}
