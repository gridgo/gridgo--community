package io.gridgo.rpc;

import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.xrpc.XrpcBuilder;
import lombok.Getter;

public class AbstractRPCTest {

    @Getter
    private final static XrpcBuilder rpcBuilder = XrpcBuilder.newBuilder(new ClasspathConnectorResolver());
}
