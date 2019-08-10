package io.gridgo.rpc.fixed;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;

public interface FixedRpcRequestUnpacker {

    static FixedRpcRequestUnpacker DEFAULT = (response) -> response.body();

    BElement unpack(Message message);
}
