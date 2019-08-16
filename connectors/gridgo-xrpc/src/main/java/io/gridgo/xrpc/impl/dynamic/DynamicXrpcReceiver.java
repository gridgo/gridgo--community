package io.gridgo.xrpc.impl.dynamic;

import static lombok.AccessLevel.PACKAGE;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.Consumer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcAckResponder;
import io.gridgo.xrpc.impl.AbstractXrpcReceiver;
import io.gridgo.xrpc.registry.XrpcReceiverRegistry;
import io.gridgo.xrpc.responder.XrpcResponderLookupable;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicXrpcReceiver extends AbstractXrpcReceiver {

    @Setter(PACKAGE)
    private @NonNull XrpcReceiverRegistry messageRegistry;

    @Setter(PACKAGE)
    private @NonNull XrpcAckResponder ackResponder;

    @Setter(PACKAGE)
    private @NonNull XrpcResponderLookupable responderRegistry;

    private boolean ack(XrpcRequestContext context, Message request) {
        var deferred = context.getOriginalDeferred();
        if (deferred != null)
            this.ackResponder.sendAck(request, deferred);
        return true;
    }

    private void onRequest(Message request, Deferred<Message, Exception> originalDeferred) {
        var context = new XrpcRequestContext();
        context.setOriginalDeferred(originalDeferred);
        var deferred = messageRegistry.registerRequest(request, context);
        publish(request, deferred);
    }

    private boolean onLookupResponder(XrpcRequestContext context, Message response) {
        log.trace("[Receiver] lookup responder: {}", context.getReplyTo());
        if (context.getReplyTo() != null) {
            var responder = responderRegistry.lookup(context.getReplyTo());
            context.setResponder(responder);
        }
        return true;
    }

    @Override
    protected void onConsumer(Consumer consumer) {
        messageRegistry.getRequestDecorators().add(0, this::ack);
        messageRegistry.getResponseDecorators().add(this::onLookupResponder);
        consumer.subscribe(this::onRequest);
    }
}
