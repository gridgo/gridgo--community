package io.gridgo.rpc.impl;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.framework.impl.NonameComponentLifecycle;
import io.gridgo.rpc.RpcMessageRegistry;
import io.gridgo.rpc.support.DataAndDeferred;

public abstract class AbstractMessageRegistry<TYPE_IN, TYPE_OUT> extends NonameComponentLifecycle
        implements RpcMessageRegistry<TYPE_IN, TYPE_OUT> {

    @Override
    public final DataAndDeferred<TYPE_OUT> registerMessage(TYPE_IN input) {
        TYPE_OUT output = translateMessage(input);
        return DataAndDeferred.<TYPE_OUT>builder() //
                .data(output) //
                .deferred(createDeferred(input, output)) //
                .build();
    }

    protected abstract TYPE_OUT translateMessage(TYPE_IN message);

    protected abstract void prepareDeferred(TYPE_IN input, TYPE_OUT output, Deferred<BElement, Exception> deferred);

    private Deferred<BElement, Exception> createDeferred(TYPE_IN input, TYPE_OUT output) {
        var deferred = new CompletableDeferredObject<BElement, Exception>();
        prepareDeferred(input, output, deferred);
        return deferred;
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }
}
