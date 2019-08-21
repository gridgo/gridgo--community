package io.gridgo.xrpc.decorator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public abstract class FieldNameDecorator implements XrpcMessageDecorator {

    @Getter
    private final @NonNull String fieldName;
}
