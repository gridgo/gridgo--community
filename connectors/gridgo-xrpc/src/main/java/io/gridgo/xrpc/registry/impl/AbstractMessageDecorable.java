package io.gridgo.xrpc.registry.impl;

import java.util.List;

import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.decorator.XrpcResponseDecorator;
import io.gridgo.xrpc.registry.XrpcMessageDecorable;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class AbstractMessageDecorable implements XrpcMessageDecorable {

    @Getter
    @Singular
    private List<XrpcRequestDecorator> requestDecorators;

    @Getter
    @Singular
    private List<XrpcResponseDecorator> responseDecorators;
}
