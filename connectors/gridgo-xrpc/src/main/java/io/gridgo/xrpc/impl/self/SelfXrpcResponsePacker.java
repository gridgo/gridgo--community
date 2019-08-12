package io.gridgo.xrpc.impl.self;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;

public interface SelfXrpcResponsePacker {

    static SelfXrpcResponsePacker BODY = Message::ofAny;
    
    Message pack(BElement body);
}
