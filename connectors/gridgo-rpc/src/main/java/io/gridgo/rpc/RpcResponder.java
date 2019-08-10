package io.gridgo.rpc;

import io.gridgo.framework.support.Message;

public interface RpcResponder<KeyType> {

    void sendResponse(KeyType key, Message response);
}
