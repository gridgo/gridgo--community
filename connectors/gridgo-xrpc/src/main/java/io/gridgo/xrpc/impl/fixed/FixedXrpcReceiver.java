package io.gridgo.xrpc.impl.fixed;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.xrpc.impl.AbstractXrpcReceiver;
import io.gridgo.xrpc.registry.XrpcReceiverRegistry;
import io.gridgo.xrpc.responder.XrpcResponder;
import io.gridgo.xrpc.responder.impl.FixedXrpcResponder;
import lombok.NonNull;

public class FixedXrpcReceiver extends AbstractXrpcReceiver {

    private @NonNull XrpcReceiverRegistry requestRegistry;

    @Override
    protected void onProducer(@NonNull Producer producer) {
        XrpcResponder responder = new FixedXrpcResponder(producer);
        requestRegistry.getResponseDecorators().add(0, (context, response) -> {
            context.setResponder(responder);
            return true;
        });
    }

    @Override
    protected void onConsumer(Consumer consumer) {
        consumer.subscribe(this::publish);
    }
}
