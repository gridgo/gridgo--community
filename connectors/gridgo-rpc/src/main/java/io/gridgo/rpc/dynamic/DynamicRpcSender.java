package io.gridgo.rpc.dynamic;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Connector;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.rpc.impl.AbstractRpcSender;
import lombok.NonNull;
import lombok.Setter;

class DynamicRpcSender extends AbstractRpcSender {

    private @NonNull DynamicRpcSenderRequestRegistry requestRegistry = new CorrIdSenderRequestRegistry();

    @NonNull
    private Producer producer;

    @Setter
    @NonNull
    private String replyEndpoint;

    @Setter
    @NonNull
    private String replyTo;

    private Connector replyConnector;

    @Override
    public Promise<BElement, Exception> send(BElement body) {
        var dataAndDeferred = this.requestRegistry.registerMessage(body);
        producer.send(dataAndDeferred.getData());
        return dataAndDeferred.getDeferred().promise();
    }

    private void onResponse(Message response) {
        requestRegistry.handleResponse(response);
    }

    @Override
    protected void onProducerReady(Producer producer) {
        this.producer = producer;

        replyConnector = resolveConnector(replyEndpoint);
        if (replyConnector == null)
            throw new RuntimeException("Connector is not available for endpoint: " + replyEndpoint);
        if (replyConnector.getConsumer().isEmpty())
            throw new RuntimeException("Consumer is not available for endpoint: " + replyEndpoint);

        replyConnector.getConsumer().get().subscribe(this::onResponse);
    }

    @Override
    protected void onConnectorStopped() {
        super.onConnectorStopped();
        replyConnector.stop();
    }
}
