package io.gridgo.xrpc.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcReceiver;
import io.gridgo.xrpc.support.SubscriberDisposable;
import lombok.NonNull;

public abstract class AbstractXrpcReceiver extends HasEndpointConnectorResolvable implements XrpcReceiver {

    private final List<BiConsumer<Message, Deferred<Message, Exception>>> consumers = new LinkedList<>();

    @Override
    public final SubscriberDisposable subscribe(@NonNull BiConsumer<Message, Deferred<Message, Exception>> consumer) {
        synchronized (consumers) {
            consumers.add(consumer);
            return () -> consumers.remove(consumer);
        }
    }

    protected final void publish(Message body, Deferred<Message, Exception> deferred) {
        consumers.forEach(consumer -> consumer.accept(body, deferred));
    }

    @Override
    protected final void onConnectorStarted(Connector connector) {
        if (connector.getConsumer().isEmpty())
            throw new RuntimeException("Consumer isn't available for endpoint: " + this.getEndpoint());

        onConsumer(connector.getConsumer().get());

        var producerOpt = connector.getProducer();
        onProducer(producerOpt.isEmpty() ? null : producerOpt.get());
    }

    protected abstract void onConsumer(Consumer consumer);

    protected void onProducer(Producer producer) {
        // do nothing
    }
}
