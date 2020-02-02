package io.gridgo.connector.jetty;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jetty.http.HttpMethod;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.jetty.impl.DefaultJettyConsumer;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.utils.support.HostAndPort;

@ConnectorEndpoint(scheme = "jetty", syntax = "http://{host}[:{port}]/[{path}]")
public class JettyConnector extends AbstractConnector {

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    @Override
    protected void onInit() {
        var host = getPlaceholder("host");
        var portStr = getPlaceholder("port");
        int port = portStr == null ? 80 : Integer.parseInt(portStr);

        var path = getPlaceholder("path");
        if (path == null || path.isBlank())
            path = "*";

        path = path.replaceAll("(?i):([a-z0-9_]*)", "{$1}");

        if (!path.startsWith("/"))
            path = "/" + path;

        var methods = Stream.of(getParam("method", "").split(",")) //
                .map(String::toUpperCase) //
                .map(HttpMethod::fromString) //
                .filter(Objects::nonNull) //
                .collect(Collectors.toList()) //
                .toArray(HttpMethod[]::new);

        var jettyConsumer = DefaultJettyConsumer.builder() //
                .path(path) //
                .methods(methods) //
                .context(getContext()) //
                .format(getParam("format", null)) //
                .pathSeparator(getParam("pathSeparator")) //
                .caseSensitiveOnMatchingPath(Boolean.valueOf(getParam("caseSensitive", FALSE))) //
                .trimTokensOnMatchingPath(Boolean.valueOf(getParam("trimTokens", TRUE))) //
                .charsetName(getParam("charset", "UTF-8")) //
                .address(HostAndPort.newInstance(host, port)) //
                .http2Enabled(Boolean.valueOf(getParam("http2Enabled", TRUE))) //
                .mmapEnabled(Boolean.valueOf(getParam("mmapEnabled", TRUE))) //
                .enablePrometheus(Boolean.valueOf(getParam("enablePrometheus", "false"))) //
                .prometheusPrefix(getParam("prometheusPrefix", "jetty")) //
                .stringBufferSize(Integer.valueOf(getParam("stringBufferSize", "65536"))) //
                .build();

        this.consumer = Optional.of(jettyConsumer);
        this.producer = Optional.of(jettyConsumer.getResponder());
    }

}
