package io.gridgo.xrpc.impl.dynamic;

import org.joo.promise4j.Deferred;

import io.gridgo.framework.support.Message;

public interface DynamicXrpcAckResponder {

    static DynamicXrpcAckResponder DEFAULT = (request, deferred) -> deferred.resolve(Message.ofAny("ack"));

    void ack(Message request, Deferred<Message, Exception> deferred);
}
