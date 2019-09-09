package io.gridgo.xrpc.support;

@FunctionalInterface
public interface SubscriberDisposable {

    boolean dispose();
}
