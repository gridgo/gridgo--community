package io.gridgo.xrpc.impl.dynamic;

import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcMessageRegistry;

public interface DynamicXrpcSenderRequestRegistry extends ComponentLifecycle, XrpcMessageRegistry {

    void setReplyTo(String replyTo);

    void handleResponse(Message response);
}
