package io.gridgo.connector.xrpc;

import java.util.Optional;

import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.xrpc.XrpcBuilder;

@ConnectorEndpoint(scheme = "xrpc", syntax = "{role}:{type}")
public class XrpcConnector extends AbstractConnector implements SenderBuilder, ReceiverBuilder {

    private static final ConnectorResolver RESOLVER = DefaultConnectorFactory.DEFAULT_CONNECTOR_RESOLVER;
    
    @Override
    protected void onInit() {
        var role = getPlaceholder("role");
        var type = getPlaceholder("type");

        var endpoint = getParamOrRegistry("endpoint", "endpointKey");

        var resolver = getResolver();
        var builder = XrpcBuilder.newBuilder(resolver);

        if (XrpcConstants.ROLE_SENDER.equals(role)) {
            var sender = createSender(type, endpoint, builder);
            this.producer = Optional.of(new XrpcSenderProducer(getContext(), sender));
        } else if (XrpcConstants.ROLE_RECEIVER.equals(role)) {
            var receiver = createReceiver(type, endpoint, builder);
            this.consumer = Optional.of(new XrpcReceiverConsumer(getContext(), receiver));
        } else {
            throw new IllegalArgumentException("Unsupported protocol: " + type);
        }
    }

    private ConnectorResolver getResolver() {
        var resolverBean = getConnectorConfig().getParameters().get("resolver");
        if (resolverBean != null) {
            return getContext().getRegistry().lookupMandatory(resolverBean.toString(), ConnectorResolver.class);
        }
        return RESOLVER;
    }

    @Override
    public String getParamOrRegistry(String paramName, String regKey) {
        var result = getParam(paramName);
        if (result != null)
            return result;
        return getContext().getRegistry().lookup(getParam(regKey), String.class);
    }
}
