package io.gridgo.rpc.support;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DataAndDeferred<DataType> {
    private DataType data;
    private Deferred<BElement, Exception> deferred;
}
