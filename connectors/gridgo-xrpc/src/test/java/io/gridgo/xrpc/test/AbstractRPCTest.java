package io.gridgo.xrpc.test;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcBuilder;
import lombok.Getter;

public class AbstractRPCTest {

    @Getter
    private final static XrpcBuilder rpcBuilder = XrpcBuilder.newBuilder(new ClasspathConnectorResolver());

    protected void echo(Message request, Deferred<Message, Exception> deferred) {
        deferred.resolve(request);
    }
}
