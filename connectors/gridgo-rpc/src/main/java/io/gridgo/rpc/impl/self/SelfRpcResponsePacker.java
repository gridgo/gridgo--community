package io.gridgo.rpc.impl.self;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;

public interface SelfRpcResponsePacker {

    static SelfRpcResponsePacker BODY = Message::ofAny;
    
    Message pack(BElement body);
}
