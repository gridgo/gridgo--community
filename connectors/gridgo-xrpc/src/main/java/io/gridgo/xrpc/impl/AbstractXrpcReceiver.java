package io.gridgo.xrpc.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.xrpc.XrpcReceiver;
import io.gridgo.xrpc.support.SubscriberDisposable;
import lombok.NonNull;

public abstract class AbstractXrpcReceiver extends HasEndpointConnectorResolvable implements XrpcReceiver {

    private final List<BiConsumer<BElement, Deferred<BElement, Exception>>> consumers = new CopyOnWriteArrayList<>();

    @Override
    public final SubscriberDisposable subscribe(@NonNull BiConsumer<BElement, Deferred<BElement, Exception>> consumer) {
        consumers.add(consumer);
        return () -> consumers.remove(consumer);
    }

    protected final void publish(BElement body, Deferred<BElement, Exception> deferred) {
        consumers.forEach(consumer -> consumer.accept(body, deferred));
    }

    @Override
    protected final void onConnectorStarted(Connector connector) {
        if (connector.getConsumer().isEmpty())
            throw new RuntimeException("Consumer isn't available for endpoint: " + this.getEndpoint());

        onConsumerReady(connector.getConsumer().get());
        connector.getProducer().ifPresent(this::onProducerReady);
    }

    protected abstract void onConsumerReady(Consumer consumer);

    protected void onProducerReady(Producer producer) {
        // do nothing
    }
}
