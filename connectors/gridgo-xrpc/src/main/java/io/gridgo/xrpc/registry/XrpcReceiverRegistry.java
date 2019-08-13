package io.gridgo.xrpc.registry;

import static org.joo.promise4j.DeferredStatus.RESOLVED;

import java.util.function.Function;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.registry.impl.DefaultReceiverRegistry;
import io.gridgo.xrpc.registry.impl.DefaultReceiverRegistry.DefaultReceiverRegistryBuilder;
import lombok.NonNull;

public interface XrpcReceiverRegistry extends XrpcMessageRegistry, XrpcMessageDecorable {

    static DefaultReceiverRegistryBuilder<?, ?> builder() {
        return DefaultReceiverRegistry.builder();
    }

    @NonNull
    Function<Exception, Message> getFailureHandler();

    @Override
    default Deferred<Message, Exception> registerRequest(Message request, XrpcRequestContext context) {
        var deferred = new CompletableDeferredObject<Message, Exception>();

        context.setDeferred(deferred);

        for (XrpcRequestDecorator decorator : this.getRequestDecorators()) {
            if (!decorator.decorateRequest(context, request))
                break;
        }

        deferred.always((stt, res, exception) -> {
            var response = stt == RESOLVED ? res : getFailureHandler().apply(exception);
            for (var decorator : getResponseDecorators())
                try {
                    if (!decorator.decorateResponse(context, response))
                        break;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

            if (context.getResponder() != null) {
                context.getResponder().sendResponse(response);
            }
        });

        return deferred;
    }
}
