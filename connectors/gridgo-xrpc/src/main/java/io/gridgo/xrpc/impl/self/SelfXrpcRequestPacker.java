package io.gridgo.xrpc.impl.self;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;

public interface SelfXrpcRequestPacker {

    static SelfXrpcRequestPacker BODY = Message::ofAny;
    
    Message packRequest(BElement body);
}
