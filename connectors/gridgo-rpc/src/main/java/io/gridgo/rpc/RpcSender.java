package io.gridgo.rpc;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.framework.ComponentLifecycle;

public interface RpcSender extends ComponentLifecycle {

    Promise<BElement, Exception> send(BElement message);
}
