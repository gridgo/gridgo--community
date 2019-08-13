package io.gridgo.xrpc.decorator;

import org.joo.promise4j.Deferred;

import io.gridgo.framework.support.Message;

public interface XrpcAckResponder {

    XrpcAckResponder DEFAULT = new XrpcAckResponder() {
    };

    default void sendAck(Message request, Deferred<Message, Exception> deferred) {
        deferred.resolve(Message.ofAny("ack"));
    }
}
