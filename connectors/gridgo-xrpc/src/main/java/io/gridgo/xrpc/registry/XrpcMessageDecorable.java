package io.gridgo.xrpc.registry;

import java.util.List;

import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.decorator.XrpcResponseDecorator;

public interface XrpcMessageDecorable {

    List<XrpcRequestDecorator> getRequestDecorators();

    List<XrpcResponseDecorator> getResponseDecorators();
}
