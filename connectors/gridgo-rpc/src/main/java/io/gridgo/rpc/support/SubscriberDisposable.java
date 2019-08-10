package io.gridgo.rpc.support;

@FunctionalInterface
public interface SubscriberDisposable {

    boolean dispose();
}
