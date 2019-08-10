package io.gridgo.rpc.fixed;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;

public interface FixedRpcResponsePacker {

    static FixedRpcResponsePacker DEFAULT = Message::ofAny;
    
    Message pack(BElement body);
}
