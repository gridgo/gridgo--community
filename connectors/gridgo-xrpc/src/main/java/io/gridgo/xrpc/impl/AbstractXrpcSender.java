package io.gridgo.xrpc.impl;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.xrpc.XrpcSender;

public abstract class AbstractXrpcSender extends HasEndpointConnectorResolvable implements XrpcSender {

    @Override
    protected final void onConnectorStarted(Connector connector) {
        if (connector.getProducer().isEmpty())
            throw new RuntimeException("Producer is not available for endpoint: " + getEndpoint());

        onProducer(connector.getProducer().get());
        connector.getConsumer().ifPresentOrElse(this::onConsumer, () -> onConsumer(null));
    }

    protected void onConsumer(Consumer consumer) {
        // do nothing
    }

    protected abstract void onProducer(Producer producer);
}
