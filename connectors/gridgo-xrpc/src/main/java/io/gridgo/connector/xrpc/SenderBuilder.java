package io.gridgo.connector.xrpc;

import io.gridgo.xrpc.XrpcBuilder;
import io.gridgo.xrpc.XrpcSender;
import lombok.NonNull;

public interface SenderBuilder {

    public default XrpcSender createSender(String type, @NonNull Object endpoint, XrpcBuilder builder) {
        if (XrpcConstants.TYPE_DYNAMIC.equals(type))
            return buildDynamicSender(endpoint, builder);
        if (XrpcConstants.TYPE_FIXED.equals(type))
            return buildFixedSender(endpoint, builder);
        if (XrpcConstants.TYPE_SELF.equals(type))
            return buildSelfSender(endpoint, builder);
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    public default XrpcSender buildSelfSender(Object endpoint, XrpcBuilder builder) {
        return builder.dynamicSender() //
                      .endpoint(endpoint.toString()) //
                      .build();
    }

    public default XrpcSender buildFixedSender(Object endpoint, XrpcBuilder builder) {
        return builder.fixedSender() //
                      .endpoint(endpoint.toString()) //
                      .build();
    }

    public default XrpcSender buildDynamicSender(Object endpoint, XrpcBuilder builder) {
        var replyTo = getParamOrRegistry("replyTo", "replyToKey");
        var replyEndpoint = getParamOrRegistry("replyEndpoint", "replyEndpointKey");

        if (replyEndpoint == null || replyTo == null)
            throw new IllegalArgumentException("Both replyEndpoint and replyTo must be non-null");
        return builder.dynamicSender() //
                      .endpoint(endpoint.toString()) //
                      .replyEndpoint(replyEndpoint.toString()) //
                      .replyTo(replyTo.toString()) //
                      .build();
    }

    public Object getParamOrRegistry(String paramName, String paramKey);

}
