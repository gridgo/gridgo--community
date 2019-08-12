package io.gridgo.rpc;

import io.gridgo.rpc.support.DataAndDeferred;

public interface RpcMessageRegistry<TYPE_IN, TYPE_OUT> {

    DataAndDeferred<TYPE_OUT> registerMessage(TYPE_IN message);
}
