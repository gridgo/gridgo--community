package io.gridgo.rpc.impl.dynamic;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;

public class RoutingIdRpcResponder extends AbstractDynamicRpcResponder<BValue> {

    @Override
    public void sendResponse(BValue key, Message response) {
        response.setRoutingId(key);
        this.getFixedResponder().send(response);
    }

    @Override
    protected BElement translateMessage(Message message) {
        return message.getPayload().toBArray();
    }

    @Override
    protected void prepareDeferred(Message input, BElement output, Deferred<BElement, Exception> deferred) {
        
    }
}
