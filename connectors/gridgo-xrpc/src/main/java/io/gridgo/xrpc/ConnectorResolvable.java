package io.gridgo.xrpc;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import lombok.NonNull;

public interface ConnectorResolvable {

    void setConnectorResolver(ConnectorResolver connectorResolver);

    ConnectorResolver getConnectorResolver();

    default Connector resolveConnector(@NonNull String url) {
        return getConnectorResolver().resolve(url);
    }
}
