package io.gridgo.xrpc.decorator;

import static lombok.AccessLevel.PROTECTED;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor(access = PROTECTED)
public abstract class FieldNameDecorator implements XrpcMessageDecorator {

    @Getter
    private final @NonNull String fieldName;
}
