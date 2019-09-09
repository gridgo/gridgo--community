package io.gridgo.xrpc.registry;

import org.joo.promise4j.Deferred;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;

public interface XrpcMessageRegistry {

    Deferred<Message, Exception> registerRequest(Message request, XrpcRequestContext context);
}
