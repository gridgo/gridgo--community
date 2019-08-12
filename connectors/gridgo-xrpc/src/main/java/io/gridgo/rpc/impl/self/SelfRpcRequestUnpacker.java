package io.gridgo.rpc.impl.self;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;

public interface SelfRpcRequestUnpacker {

    static SelfRpcRequestUnpacker BODY = (response) -> response.body();

    BElement unpack(Message message);
}
