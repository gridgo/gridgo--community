package io.gridgo.rpc.impl;

import static lombok.AccessLevel.PROTECTED;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Producer;
import io.gridgo.rpc.RpcSender;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractRpcSender extends HasEndpointConnectorResolvable implements RpcSender {

    @Getter(PROTECTED)
    private @NonNull Producer producer;

    protected abstract void onProducerReady(Producer producer);

    @Override
    protected final void onConnectorStarted(Connector connector) {
        if (connector.getProducer().isEmpty())
            throw new RuntimeException("Producer is not available for endpoint: " + getEndpoint());

        this.onProducerReady(connector.getProducer().get());
    }
}
