package io.gridgo.xrpc.impl.dynamic;

import io.gridgo.connector.Consumer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcAckResponder;
import io.gridgo.xrpc.impl.AbstractXrpcReceiver;
import io.gridgo.xrpc.registry.XrpcReceiverRegistry;
import lombok.NonNull;

public class DynamicXrpcReceiver extends AbstractXrpcReceiver {

    private @NonNull XrpcReceiverRegistry messageRegistry;

    private @NonNull XrpcAckResponder ackResponder;

    private boolean ack(XrpcRequestContext context, Message request) {
        var deferred = context.getOriginalDeferred();
        if (deferred != null)
            this.ackResponder.sendAck(request, deferred);
        return true;
    }

    @Override
    protected void onConsumer(Consumer consumer) {
        messageRegistry.getRequestDecorators().add(0, this::ack);
        consumer.subscribe((request, originalDeferred) -> {
            var context = new XrpcRequestContext();
            context.setOriginalDeferred(originalDeferred);
            messageRegistry.registerRequest(request, context);
        });
    }
}
