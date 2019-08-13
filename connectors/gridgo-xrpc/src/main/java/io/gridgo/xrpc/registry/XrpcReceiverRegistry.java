package io.gridgo.xrpc.registry;

import java.util.function.Function;

import org.joo.promise4j.Deferred;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
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
    Deferred<Message, Exception> registerRequest(Message request, XrpcRequestContext context);
}
