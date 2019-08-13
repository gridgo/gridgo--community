package io.gridgo.xrpc.decorator;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class FieldNameDecorator implements XrpcMessageDecorator {

    @Getter
    private @NonNull String fieldName;
}
