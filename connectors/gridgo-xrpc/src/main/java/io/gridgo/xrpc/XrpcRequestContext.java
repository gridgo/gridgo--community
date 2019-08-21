package io.gridgo.xrpc;

import org.joo.promise4j.Deferred;

import io.gridgo.framework.support.Message;
import io.gridgo.utils.wrapper.ByteArray;
import io.gridgo.xrpc.responder.XrpcResponder;
import lombok.Data;

@Data
public class XrpcRequestContext {

    private ByteArray corrId;

    private String replyTo;

    private Deferred<Message, Exception> originalDeferred;

    private Deferred<Message, Exception> deferred;

    private XrpcResponder responder;

}
