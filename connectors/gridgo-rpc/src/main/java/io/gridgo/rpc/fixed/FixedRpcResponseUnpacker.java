package io.gridgo.rpc.fixed;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;

public interface FixedRpcResponseUnpacker {

    static FixedRpcResponseUnpacker DEFAULT = (response) -> response.body();

    BElement unpack(Message message);
}
