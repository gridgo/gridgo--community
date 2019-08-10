package io.gridgo.rpc.fixed;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;

public interface FixedRpcRequestPacker {

    static FixedRpcRequestPacker DEFAULT = Message::ofAny;
    
    Message packRequest(BElement body);
}
