package io.gridgo.xrpc.decorator;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;

public interface XrpcResponseDecorator extends XrpcMessageDecorator {

    boolean decorateResponse(XrpcRequestContext context, Message response);
}
