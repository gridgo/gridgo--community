package io.gridgo.xrpc.impl.dynamic;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcConnectorResolvable;
import io.gridgo.xrpc.impl.AbstractXrpcReceiver;
import lombok.NonNull;
import lombok.Setter;

public class DynamicXrpcReceiver extends AbstractXrpcReceiver {

    @Setter
    private @NonNull DynamicXrpcResponder<?> responder = new CorrIdXrpcResponder();

    @Setter
    private @NonNull DynamicXrpcAckResponder ackResponder = DynamicXrpcAckResponder.DEFAULT;

    @Override
    protected void onConsumerReady(Consumer consumer) {
        if (responder instanceof XrpcConnectorResolvable) {
            ((XrpcConnectorResolvable) responder).setConnectorResolver(getConnectorResolver());
        }
        responder.start();

        consumer.subscribe(this::onRequest);
    }

    @Override
    protected void onProducerReady(Producer producer) {
        responder.setFixedResponder(producer);
    }

    @Override
    protected void onConnectorStopped() {
        if (responder != null)
            responder.stop();
    }

    private void onRequest(Message request, Deferred<Message, Exception> deferred) {
        if (deferred != null) {
            ackResponder.ack(request, deferred);
        }
        var internalDeferred = responder.registerMessage(request);
        publish(request, internalDeferred);
    }
}
