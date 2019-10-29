package io.gridgo.xrpc.registry.impl;

import java.util.function.Function;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.registry.XrpcReceiverRegistry;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractReceiverRegistry extends AbstractMessageRegistry implements XrpcReceiverRegistry {

    @Setter
    @Getter
    private @NonNull Function<Exception, Message> failureHandler;

    @Override
    public Deferred<Message, Exception> registerRequest(Message request, XrpcRequestContext context) {
        var deferred = new CompletableDeferredObject<Message, Exception>();

        context.setDeferred(deferred);

        for (XrpcRequestDecorator decorator : this.getRequestDecorators())
            if (!decorator.decorateRequest(context, request))
                break;

        deferred.pipeFail(this::mapError) //
                .then(response -> {
                    decorateResponse(context, response);
                    sendResponse(context, response);
                    return Promise.of(null);
                }).fail(ex -> {
                    log.error("Exception caught while trying to send response", ex);
                });
        return deferred;
    }

    protected Promise<Message, Throwable> mapError(Exception ex) {
        return Promise.of(getFailureHandler().apply(ex));
    }

    protected void decorateResponse(XrpcRequestContext context, Message response) {
        for (var decorator : getResponseDecorators())
            if (!decorator.decorateResponse(context, response))
                break;
    }

    protected void sendResponse(XrpcRequestContext context, Message response) {
        if (context.getResponder() != null)
            context.getResponder().sendResponse(response);
    }
}
