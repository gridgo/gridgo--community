package io.gridgo.connector.jetty.server;

import static io.gridgo.connector.jetty.support.PathMatcher.DEFAULT_CASE_SENSITIVE;
import static io.gridgo.utils.ThreadUtils.registerShutdownTask;

import java.util.Map;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.gridgo.connector.jetty.support.PathMatcher;
import io.gridgo.utils.support.HostAndPort;
import lombok.Getter;

public class JettyHttpServerManager {

    @Getter
    private static final JettyHttpServerManager instance = new JettyHttpServerManager();

    private static final String ALL_INTERFACE_HOST = "0.0.0.0";

    private final Map<HostAndPort, JettyHttpServer> servers = new NonBlockingHashMap<>();

    private JettyHttpServerManager() {
        registerShutdownTask(this::onShutdown);
    }

    private void onShutdown() {
        servers.values().forEach(JettyHttpServer::stop);
    }

    public JettyHttpServer getOrCreateJettyServer(String originAddress, boolean http2Enabled) {
        return getOrCreateJettyServer(HostAndPort.fromString(originAddress), http2Enabled);
    }

    public JettyHttpServer getOrCreateJettyServer(String originAddress, boolean http2Enabled, PathMatcher pathMatcher) {
        return getOrCreateJettyServer(HostAndPort.fromString(originAddress), http2Enabled, pathMatcher);
    }

    public JettyHttpServer getOrCreateJettyServer(HostAndPort originAddress, boolean http2Enabled) {
        return getOrCreateJettyServer(originAddress, http2Enabled, DEFAULT_CASE_SENSITIVE);
    }

    public synchronized JettyHttpServer getOrCreateJettyServer(HostAndPort originAddress, boolean http2Enabled,
            PathMatcher pathMatcher) {

        var address = originAddress.makeCopy();
        if (!address.isResolvable())
            throw new RuntimeException("Host '" + originAddress.getHost() + "' cannot be resolved");

        if (address.getPort() <= 0)
            address.setPort(80);

        if (address.getHost() == null)
            address.setHost("localhost");

        var jettyHttpServer = servers.get(address);
        if (jettyHttpServer != null)
            return jettyHttpServer;

        var allInterface = HostAndPort.newInstance(ALL_INTERFACE_HOST, address.getPort());
        jettyHttpServer = servers.get(allInterface);

        if (jettyHttpServer != null)
            return jettyHttpServer;

        return servers.computeIfAbsent( //
                address, //
                _address -> JettyHttpServer.builder() //
                        .address(_address) //
                        .pathMatcher(pathMatcher) //
                        .http2Enabled(http2Enabled) //
                        .onStopCallback(servers::remove) //
                        .build());
    }
}
