package io.gridgo.xrpc.registry.impl;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.registry.XrpcSenderRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractSenderRegistry extends AbstractMessageRegistry implements XrpcSenderRegistry {

    @Override
    public Deferred<Message, Exception> registerRequest(Message request, XrpcRequestContext context) {
        context.setDeferred(new CompletableDeferredObject<Message, Exception>());
        for (var decorator : getRequestDecorators())
            if (!decorator.decorateRequest(context, request)) {
                if (log.isDebugEnabled())
                    log.debug("stop decorating request because previous decorator return false");
                break;
            }

        return context.getDeferred();
    }

    @Override
    public void resolveResponse(Message response) {
        var context = new XrpcRequestContext();

        try {
            for (var decorator : getResponseDecorators())
                if (!decorator.decorateResponse(context, response)) {
                    if (log.isDebugEnabled())
                        log.debug("stop decorating response because previous decorator return false");
                    break;
                }
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Error while decorating response", e);

            if (context.getDeferred() != null) {
                context.getDeferred().reject(e);
                return;
            } else {
                throw e;
            }
        }

        if (context.getDeferred() != null)
            context.getDeferred().resolve(response);
    }
}
