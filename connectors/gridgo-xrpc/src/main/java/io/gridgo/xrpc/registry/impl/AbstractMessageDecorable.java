package io.gridgo.xrpc.registry.impl;

import java.util.LinkedList;
import java.util.List;

import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.decorator.XrpcResponseDecorator;
import io.gridgo.xrpc.registry.XrpcMessageDecorable;
import lombok.Getter;
import lombok.Singular;

public class AbstractMessageDecorable implements XrpcMessageDecorable {

    @Getter
    @Singular
    private final List<XrpcRequestDecorator> requestDecorators = new LinkedList<>();

    @Getter
    @Singular
    private final List<XrpcResponseDecorator> responseDecorators = new LinkedList<>();
}
