package io.gridgo.xrpc;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.framework.ComponentLifecycle;

public interface XrpcSender extends ComponentLifecycle {

    Promise<BElement, Exception> send(BElement message);
}
