package io.gridgo.xrpc.responder.impl;

import io.gridgo.connector.Producer;
import io.gridgo.xrpc.responder.XrpcFixedResponder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class FixedXrpcResponder extends AbstractXrpcResponder implements XrpcFixedResponder {

    @Getter
    @Setter
    private Producer fixedResponder;
}
