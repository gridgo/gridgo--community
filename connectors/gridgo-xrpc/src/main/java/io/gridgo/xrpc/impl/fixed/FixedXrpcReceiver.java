package io.gridgo.xrpc.impl.fixed;

import static lombok.AccessLevel.PROTECTED;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.impl.AbstractXrpcReceiver;
import io.gridgo.xrpc.registry.XrpcReceiverRegistry;
import io.gridgo.xrpc.responder.XrpcResponder;
import io.gridgo.xrpc.responder.impl.FixedXrpcResponder;
import lombok.NonNull;
import lombok.Setter;

public class FixedXrpcReceiver extends AbstractXrpcReceiver {

    @Setter(PROTECTED)
    private @NonNull XrpcReceiverRegistry requestRegistry;

    @Override
    protected void onProducer(@NonNull Producer producer) {
        XrpcResponder responder = new FixedXrpcResponder(producer);
        requestRegistry.getResponseDecorators().add(0, (context, response) -> {
            context.setResponder(responder);
            return true;
        });
    }

    private void onRequest(Message request, Deferred<Message, Exception> originalDeferred) {
        var context = new XrpcRequestContext();
        context.setOriginalDeferred(originalDeferred);
        var deferred = requestRegistry.registerRequest(request, context);
        publish(request, deferred);
    }

    @Override
    protected void onConsumer(Consumer consumer) {
        consumer.subscribe(this::onRequest);
    }
}
