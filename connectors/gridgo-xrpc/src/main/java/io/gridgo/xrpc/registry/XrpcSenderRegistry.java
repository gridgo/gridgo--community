package io.gridgo.xrpc.registry;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;

public interface XrpcSenderRegistry extends XrpcMessageRegistry, XrpcMessageDecorable, XrpcResponseResolvable {

    @Override
    default Deferred<Message, Exception> registerRequest(Message request, XrpcRequestContext context) {
        context.setDeferred(new CompletableDeferredObject<Message, Exception>());
        for (var decorator : getRequestDecorators())
            if (!decorator.decorateRequest(context, request))
                break;
        return context.getDeferred();
    }

    @Override
    default void resolveResponse(Message response) {
        var context = new XrpcRequestContext();
        for (var decorator : getResponseDecorators())
            if (!decorator.decorateResponse(context, response))
                break;
        if (context.getDeferred() != null)
            context.getDeferred().resolve(response);
    }
}
