package io.gridgo.xrpc.impl;

import static lombok.AccessLevel.PROTECTED;

import io.gridgo.connector.Connector;
import lombok.Getter;
import lombok.Setter;

public abstract class HasEndpointConnectorResolvable extends AbstractConnectorResolvable {

    @Setter
    @Getter(PROTECTED)
    private String endpoint;

    private Connector connector;

    protected void onConnectorStarted(Connector connector) {
        // do nothing
    }

    protected void onConnectorStopped() {
        // do nothing
    }

    @Override
    protected final void onStart() {
        this.connector = this.resolveConnector(endpoint);
        if (connector == null)
            throw new RuntimeException("Connector cannot be resolved from endpoint: " + endpoint);
        connector.start();

        this.onConnectorStarted(connector);
    }

    @Override
    protected final void onStop() {
        this.connector.stop();
        this.onConnectorStopped();
    }
}
