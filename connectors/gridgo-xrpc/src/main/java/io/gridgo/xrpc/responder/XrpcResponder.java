package io.gridgo.xrpc.responder;

import io.gridgo.framework.support.Message;

public interface XrpcResponder {

    void sendResponse(Message response);
}
