package io.gridgo.xrpc;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.framework.support.Message;

public interface XrpcSender extends ComponentLifecycle {

    Promise<Message, Exception> call(Message message);

    default Promise<Message, Exception> call(BElement body) {
        return call(Message.ofAny(body));
    }
}
