package io.gridgo.xrpc.impl.fixed;

import static lombok.AccessLevel.PACKAGE;

import org.joo.promise4j.Promise;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.impl.AbstractXrpcSender;
import io.gridgo.xrpc.registry.XrpcSenderRegistry;
import lombok.NonNull;
import lombok.Setter;

public class FixedXrpcSender extends AbstractXrpcSender {

    private Producer producer;

    @Setter(PACKAGE)
    private XrpcSenderRegistry messageRegistry;

    @Override
    public Promise<Message, Exception> call(Message message) {
        var deferred = messageRegistry.registerRequest(message, new XrpcRequestContext());
        this.producer.send(message);
        return deferred.promise();
    }

    @Override
    protected void onConsumer(@NonNull Consumer consumer) {
        consumer.subscribe(messageRegistry::resolveResponse);
    }

    @Override
    protected void onProducer(@NonNull Producer producer) {
        this.producer = producer;
    }
}
