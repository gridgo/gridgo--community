package io.gridgo.connector.xrpc;

import io.gridgo.xrpc.XrpcBuilder;
import io.gridgo.xrpc.XrpcReceiver;

public interface ReceiverBuilder {

    public default XrpcReceiver createReceiver(String type, String endpoint, XrpcBuilder builder) {
        if (XrpcConstants.TYPE_DYNAMIC.equals(type)) {
            return builder.dynamicReceiver() //
                          .endpoint(endpoint.toString()) //
                          .build();
        }
        if (XrpcConstants.TYPE_FIXED.equals(type)) {
            return builder.fixedReceiver() //
                          .endpoint(endpoint.toString()) //
                          .build();
        }
        if (XrpcConstants.TYPE_SELF.equals(type)) {
            return builder.selfReceiver() //
                          .endpoint(endpoint.toString()) //
                          .build();
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }
}
