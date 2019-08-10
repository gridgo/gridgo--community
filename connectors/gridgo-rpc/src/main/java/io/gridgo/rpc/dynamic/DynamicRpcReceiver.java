package io.gridgo.rpc.dynamic;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.Consumer;
import io.gridgo.framework.support.Message;
import io.gridgo.rpc.impl.AbstractRpcReceiver;
import lombok.NonNull;
import lombok.Setter;

public class DynamicRpcReceiver extends AbstractRpcReceiver {

    @Setter
    private @NonNull DynamicRpcResponder<?> responder = new CorrIdRpcResponder();

    @Setter
    private @NonNull DynamicRpcAckResponder ackResponder = DynamicRpcAckResponder.DEFAULT;

    @Override
    protected void onConsumerReady(Consumer consumer) {
        if (responder != null) {
            responder.setConnectorResolver(getConnectorResolver());
            responder.start();
        }

        consumer.subscribe(this::onRequest);
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
        var unpacked = responder.registerMessage(request);
        this.publish(unpacked.getData(), unpacked.getDeferred());
    }
}
