package io.gridgo.xrpc.impl.dynamic;

import static lombok.AccessLevel.PROTECTED;

import org.joo.promise4j.Promise;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.exception.XrpcException;
import io.gridgo.xrpc.impl.AbstractXrpcSender;
import io.gridgo.xrpc.registry.XrpcSenderRegistry;
import lombok.NonNull;
import lombok.Setter;

public class DynamicXrpcSender extends AbstractXrpcSender {

    private Producer producer;

    @Setter(PROTECTED)
    private @NonNull String replyEndpoint;

    @Setter(PROTECTED)
    private XrpcSenderRegistry messageRegistry;

    private Connector replyConnector;

    @Override
    public Promise<Message, Exception> call(Message message) {
        try {
            var deferred = messageRegistry.registerRequest(message, new XrpcRequestContext());
            if (deferred == null)
                return Promise.ofCause(new XrpcException("Request cannot be made, internal connector error"));

            producer.sendWithAck(message).pipeFail(ex -> {
                deferred.reject(ex);
                return Promise.ofCause(ex);
            });
            return deferred.promise();
        } catch (Exception e) {
            return Promise.ofCause(e);
        }
    }

    private void onReplyConsumer(Consumer replyConsumer) {
        replyConsumer.subscribe(messageRegistry::resolveResponse);
    }

    private void onReplyConsumerUnavailable() {
        throw new XrpcException("Consumer isn't available for endpoint: " + replyEndpoint);
    }

    @Override
    protected void onConsumer(Consumer consumer) {
        replyConnector = resolveConnector(replyEndpoint);
        if (replyConnector == null)
            throw new XrpcException("Reply connector cannot be resolved from endpoint: " + replyEndpoint);

        replyConnector.start();
        replyConnector.getConsumer().ifPresentOrElse(this::onReplyConsumer, this::onReplyConsumerUnavailable);
    }

    @Override
    protected void onProducer(@NonNull Producer producer) {
        this.producer = producer;
    }

    @Override
    protected void onConnectorStopped() {
        super.onConnectorStopped();
        this.replyConnector.stop();
    }
}
