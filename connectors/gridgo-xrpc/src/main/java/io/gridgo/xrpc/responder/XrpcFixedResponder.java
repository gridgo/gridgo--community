package io.gridgo.xrpc.responder;

import org.joo.promise4j.Promise;

import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import lombok.NonNull;

public interface XrpcFixedResponder extends XrpcResponder {

    @NonNull
    Producer getFixedResponder();

    @Override
    default void sendResponse(Message response) {
        try {
            this.getFixedResponder().sendWithAck(response).pipeFail(ex -> {
                ex.printStackTrace();
                return Promise.ofCause(ex);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
