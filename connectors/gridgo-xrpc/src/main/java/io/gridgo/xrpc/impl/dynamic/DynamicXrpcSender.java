package io.gridgo.xrpc.impl.dynamic;

import org.joo.promise4j.Promise;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.impl.AbstractXrpcSender;
import lombok.NonNull;
import lombok.Setter;

class DynamicXrpcSender extends AbstractXrpcSender {

    private @NonNull DynamicXrpcSenderRequestRegistry requestRegistry = new CorrIdSenderRequestRegistry();

    @NonNull
    private Producer producer;

    @Setter
    private String replyEndpoint;

    @Setter
    private String replyTo;

    private Connector replyConnector;

    @Override
    public Promise<Message, Exception> send(Message request) {
        var deferred = requestRegistry.registerMessage(request);
        producer.send(request);
        return deferred.promise();
    }

    private void onResponse(Message response) {
        requestRegistry.handleResponse(response);
    }

    @Override
    protected void onConsumerReady(Consumer consumer) {
        consumer.subscribe(this::onResponse);
    }

    @Override
    protected void onProducerReady(Producer producer) {
        this.producer = producer;

        this.requestRegistry.setReplyTo(replyTo);

        if (replyEndpoint != null) {
            replyConnector = resolveConnector(replyEndpoint);
            if (replyConnector == null)
                throw new RuntimeException("Connector is not available for endpoint: " + replyEndpoint);

            replyConnector.start();

            if (replyConnector.getConsumer().isEmpty())
                throw new RuntimeException("Consumer is not available for endpoint: " + replyEndpoint);

            replyConnector.getConsumer().get().subscribe(this::onResponse);
        }
    }

    @Override
    protected void onConnectorStopped() {
        super.onConnectorStopped();
        if (replyConnector != null)
            replyConnector.stop();
    }
}
