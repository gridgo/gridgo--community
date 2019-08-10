package io.gridgo.rpc.dynamic;

import io.gridgo.bean.BElement;
import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.framework.support.Message;
import io.gridgo.rpc.RpcMessageRegistry;

public interface DynamicRpcSenderRequestRegistry extends ComponentLifecycle, RpcMessageRegistry<BElement, Message> {

    void handleResponse(Message response);
}
