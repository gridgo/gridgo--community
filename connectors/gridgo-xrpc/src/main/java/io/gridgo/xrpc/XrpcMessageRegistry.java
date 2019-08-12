package io.gridgo.xrpc;

import org.joo.promise4j.Deferred;

import io.gridgo.framework.support.Message;

public interface XrpcMessageRegistry {

    Deferred<Message, Exception> registerMessage(Message message);
}
