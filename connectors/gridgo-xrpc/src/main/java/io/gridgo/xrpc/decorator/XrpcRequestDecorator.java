package io.gridgo.xrpc.decorator;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;

public interface XrpcRequestDecorator extends XrpcMessageDecorator {

    boolean decorateRequest(XrpcRequestContext context, Message request);
}
