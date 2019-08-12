package io.gridgo.xrpc.impl.self;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;

public interface SelfXrpcResponseUnpacker {

    static SelfXrpcResponseUnpacker BODY = (response) -> response.body();

    BElement unpack(Message message);
}
