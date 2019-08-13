package io.gridgo.xrpc.registry;

import static org.joo.promise4j.DeferredStatus.RESOLVED;

import java.util.function.Function;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import lombok.NonNull;

public interface XrpcReceiverRegistry extends XrpcMessageRegistry, XrpcMessageDecorable {

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
            getResponseDecorators().forEach(decorator -> decorator.decorateResponse(context, response));
            if (context.getResponder() != null) {
                context.getResponder().sendResponse(response);
            }
        });

        return deferred;
    }
}
