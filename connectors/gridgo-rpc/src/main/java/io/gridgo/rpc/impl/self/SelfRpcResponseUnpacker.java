package io.gridgo.rpc.impl.self;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;

public interface SelfRpcResponseUnpacker {

    static SelfRpcResponseUnpacker BODY = (response) -> response.body();

    BElement unpack(Message message);
}
