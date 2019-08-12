package io.gridgo.xrpc;

import io.gridgo.xrpc.support.DataAndDeferred;

public interface XrpcMessageRegistry<TYPE_IN, TYPE_OUT> {

    DataAndDeferred<TYPE_OUT> registerMessage(TYPE_IN message);
}
