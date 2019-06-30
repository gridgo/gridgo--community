package io.gridgo.extras.consul;

import java.io.IOException;
import java.io.InputStream;

import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration.RegCheck;

import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.impl.PropertiesFileRegistry;

public class ConsulCheckinComponentFactory {

    private static final String DEFAULT_CONSUL_FILE = "consul.properties";

    public static ConsulCheckinComponent ofResource() {
        return ofResource(DEFAULT_CONSUL_FILE);
    }

    public static ConsulCheckinComponent ofResource(String consulFile) {
        var classloader = Thread.currentThread().getContextClassLoader();
        return ofStream(classloader.getResourceAsStream(consulFile));
    }

    public static ConsulCheckinComponent ofStream(InputStream consulFileStream) {
        try {
            var registry = new PropertiesFileRegistry(consulFileStream);
            return ofRegistry(registry);
        } finally {
            closeSilently(consulFileStream);
        }
    }

    public static ConsulCheckinComponent ofRegistry(Registry registry) {
        var id = registry.lookup("consul.service.id", String.class);
        var name = registry.lookup("consul.service.name", String.class);
        var addr = registry.lookup("consul.service.addr", String.class);
        var port = registry.lookup("consul.service.port", Integer.class);
        var ttl = registry.lookup("consul.ttl", Long.class);
        var builder = ImmutableRegistration.builder().id(id).name(name);
        if (addr != null)
            builder.address(addr);
        if (port != null)
            builder.port(port);
        if (ttl != null && ttl > 0)
            builder.check(RegCheck.ttl(ttl));

        var heartbeat = registry.lookup("consul.heartbeat", Long.class);
        var url = registry.lookup("consul.url", String.class);
        if (heartbeat <= 0 && ttl != null && ttl > 0) {
            throw new IllegalArgumentException("Consul heartbeat must be greater than zero if TTL is not zero");
        }

        return new ConsulCheckinComponent(builder.build(), heartbeat, url);
    }

    private static void closeSilently(InputStream consulFileStream) {
        try {
            consulFileStream.close();
        } catch (IOException e) {
            // Nothing to do here
        }
    }
}
