package io.gridgo.connector.xrpc;

import java.util.Optional;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.xrpc.XrpcBuilder;
import io.gridgo.xrpc.XrpcReceiver;
import io.gridgo.xrpc.XrpcSender;
import lombok.NonNull;

@ConnectorEndpoint(scheme = "xrpc", syntax = "{role}:{type}")
public class XrpcConnector extends AbstractConnector {

    private static final ConnectorResolver RESOLVER = DefaultConnectorFactory.DEFAULT_CONNECTOR_RESOLVER;

    @Override
    protected void onInit() {
        var role = getPlaceholder("role");
        var type = getPlaceholder("type");

        var endpoint = getParamOrRegistry("endpoint", "endpointKey");
        var replyTo = getParamOrRegistry("replyTo", "replyToKey");
        var replyEndpoint = getParamOrRegistry("replyEndpoint", "replyEndpointKey");

        var resolver = getResolver();
        var builder = XrpcBuilder.newBuilder(resolver);

        if (XrpcConstants.ROLE_SENDER.equals(role)) {
            var sender = createSender(type, endpoint, replyTo, replyEndpoint, builder);
            this.producer = Optional.of(new XrpcSenderProducer(getContext(), sender));
        } else if (XrpcConstants.ROLE_RECEIVER.equals(role)) {
            var receiver = createReceiver(type, endpoint, builder);
            this.consumer = Optional.of(new XrpcReceiverConsumer(getContext(), receiver));
        } else {
            throw new IllegalArgumentException("Unsupported protocol: " + type);
        }
    }

    private String getParamOrRegistry(String paramName, String regKey) {
        var result = getParam(paramName);
        if (result != null)
            return result;
        return getContext().getRegistry().lookup(getParam(regKey), String.class);
    }

    private XrpcReceiver createReceiver(String type, String endpoint, XrpcBuilder builder) {
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

    private XrpcSender createSender(String type, @NonNull Object endpoint, Object replyTo, Object replyEndpoint,
            XrpcBuilder builder) {
        if (XrpcConstants.TYPE_DYNAMIC.equals(type)) {
            if (replyEndpoint == null || replyTo == null)
                throw new IllegalArgumentException("Both replyEndpoint and replyTo must be non-null");
            return builder.dynamicSender() //
                          .endpoint(endpoint.toString()) //
                          .replyEndpoint(replyEndpoint.toString()) //
                          .replyTo(replyTo.toString()) //
                          .build();
        }
        if (XrpcConstants.TYPE_FIXED.equals(type)) {
            return builder.fixedSender() //
                          .endpoint(endpoint.toString()) //
                          .build();
        }
        if (XrpcConstants.TYPE_SELF.equals(type)) {
            return builder.dynamicSender() //
                          .endpoint(endpoint.toString()) //
                          .build();
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    private ConnectorResolver getResolver() {
        var resolverBean = getConnectorConfig().getParameters().get("resolver");
        var resolver = RESOLVER;
        if (resolverBean != null) {
            resolver = getContext().getRegistry().lookupMandatory(resolverBean.toString(), ConnectorResolver.class);
        }
        return resolver;
    }
}
