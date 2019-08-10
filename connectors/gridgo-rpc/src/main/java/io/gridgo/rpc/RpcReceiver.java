package io.gridgo.rpc;

import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.rpc.support.SubscriberDisposable;

public interface RpcReceiver extends ComponentLifecycle {

    SubscriberDisposable subscribe(BiConsumer<BElement, Deferred<BElement, Exception>> consumer);
}
