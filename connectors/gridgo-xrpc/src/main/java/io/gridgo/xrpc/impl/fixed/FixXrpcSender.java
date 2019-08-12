package io.gridgo.xrpc.impl.fixed;

import org.joo.promise4j.Promise;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcMessageRegistry;
import io.gridgo.xrpc.impl.AbstractXrpcSender;

public class FixXrpcSender extends AbstractXrpcSender {

    private Producer producer;

    private XrpcMessageRegistry messageRegistry;

    @Override
    public Promise<Message, Exception> call(Message message) {
        var deferred = messageRegistry.registerMessage(message);
        this.producer.send(message);
        return deferred.promise();
    }

    private void onResponse(Message response) {
        
    }

    @Override
    protected void onConsumerReady(Consumer consumer) {
        if (consumer == null)
            throw new RuntimeException("Consumer is not available for endpoint: " + this.getEndpoint());
        consumer.subscribe(this::onResponse);
    }

    @Override
    protected void onProducerReady(Producer producer) {
        this.producer = producer;
    }
}
