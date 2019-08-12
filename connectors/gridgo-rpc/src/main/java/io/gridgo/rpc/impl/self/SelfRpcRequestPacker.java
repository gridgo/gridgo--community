package io.gridgo.rpc.impl.self;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;

public interface SelfRpcRequestPacker {

    static SelfRpcRequestPacker BODY = Message::ofAny;
    
    Message packRequest(BElement body);
}
