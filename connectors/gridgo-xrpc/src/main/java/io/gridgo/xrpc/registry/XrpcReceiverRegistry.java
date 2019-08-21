package io.gridgo.xrpc.registry;

import java.util.function.Function;

import org.joo.promise4j.Deferred;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import lombok.NonNull;

public interface XrpcReceiverRegistry extends XrpcMessageRegistry, XrpcMessageDecorable {

    @NonNull
    Function<Exception, Message> getFailureHandler();

    @Override
    Deferred<Message, Exception> registerRequest(Message request, XrpcRequestContext context);
}
