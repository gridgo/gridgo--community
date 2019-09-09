package io.gridgo.xrpc.decorator;

public interface XrpcMessageCodec extends XrpcRequestDecorator, XrpcResponseDecorator {

    default XrpcRequestDecorator getRequestDecorator() {
        return this::decorateRequest;
    }

    default XrpcResponseDecorator getResponseDecorator() {
        return this::decorateResponse;
    }
}
