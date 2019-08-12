package io.gridgo.xrpc.impl;

import static lombok.AccessLevel.PROTECTED;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.xrpc.XrpcSender;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractXrpcSender extends HasEndpointConnectorResolvable implements XrpcSender {

    @Getter(PROTECTED)
    private @NonNull Producer producer;

    protected abstract void onProducerReady(Producer producer);

    @Override
    protected final void onConnectorStarted(Connector connector) {
        if (connector.getProducer().isEmpty())
            throw new RuntimeException("Producer is not available for endpoint: " + getEndpoint());

        onProducerReady(connector.getProducer().get());
        connector.getConsumer().ifPresentOrElse(this::onConsumerReady, () -> this.onConsumerReady(null));
    }

    protected void onConsumerReady(Consumer consumer) {
        // do nothing
    }
}
