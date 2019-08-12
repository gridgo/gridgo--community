package io.gridgo.rpc.impl;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.framework.impl.NonameComponentLifecycle;
import io.gridgo.rpc.ConnectorResolvable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractConnectorResolvable extends NonameComponentLifecycle implements ConnectorResolvable {

    @Setter
    @Getter
    private @NonNull ConnectorResolver connectorResolver;

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }
}
