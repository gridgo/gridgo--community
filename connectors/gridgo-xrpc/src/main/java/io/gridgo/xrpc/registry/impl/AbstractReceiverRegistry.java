package io.gridgo.xrpc.registry.impl;

import java.util.function.Function;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.registry.XrpcReceiverRegistry;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class AbstractReceiverRegistry extends AbstractMessageRegistry implements XrpcReceiverRegistry {

    @Getter
    private @NonNull Function<Exception, Message> failureHandler;
}
