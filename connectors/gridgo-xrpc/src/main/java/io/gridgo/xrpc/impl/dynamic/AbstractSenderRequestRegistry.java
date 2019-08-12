package io.gridgo.xrpc.impl.dynamic;

import static lombok.AccessLevel.PROTECTED;

import io.gridgo.xrpc.impl.AbstractMessageRegistry;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractSenderRequestRegistry extends AbstractMessageRegistry
        implements DynamicXrpcSenderRequestRegistry {

    @Setter
    @Getter(PROTECTED)
    private String replyTo;
}
