package io.gridgo.xrpc;

import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.xrpc.support.SubscriberDisposable;

public interface XrpcReceiver extends ComponentLifecycle {

    SubscriberDisposable subscribe(BiConsumer<BElement, Deferred<BElement, Exception>> consumer);
}
