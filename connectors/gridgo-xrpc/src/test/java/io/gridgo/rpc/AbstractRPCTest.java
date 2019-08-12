package io.gridgo.rpc;

import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import lombok.Getter;

public class AbstractRPCTest {

    @Getter
    private final static RpcBuilder rpcBuilder = RpcBuilder.newBuilder(new ClasspathConnectorResolver());
}
