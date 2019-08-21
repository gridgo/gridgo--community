package io.gridgo.xrpc.decorator.corrid;

import static lombok.AccessLevel.PROTECTED;

import java.util.Map;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.decorator.FieldNameDecorator;
import lombok.Getter;
import lombok.NonNull;

public abstract class CorrIdSenderDecorator extends FieldNameDecorator {

    @Getter(PROTECTED)
    private final @NonNull Map<BValue, Deferred<Message, Exception>> deferredCache;

    public CorrIdSenderDecorator(String fieldName, Map<BValue, Deferred<Message, Exception>> deferredCache) {
        super(fieldName);
        this.deferredCache = deferredCache;
    }
}
