package io.gridgo.xrpc.registry;

import io.gridgo.framework.support.Message;

public interface XrpcResponseResolvable {

    void resolveResponse(Message response);
}
