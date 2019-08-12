package io.gridgo.xrpc;

import io.gridgo.framework.support.Message;

public interface XrpcResponder<KeyType> {

    void sendResponse(KeyType key, Message response);
}
