package io.gridgo.xrpc.responder;

import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import lombok.NonNull;

public interface XrpcFixedResponder extends XrpcResponder {

    @NonNull
    Producer getFixedResponder();

    @Override
    default void sendResponse(Message response) {
        this.getFixedResponder().send(response);
    }
}
