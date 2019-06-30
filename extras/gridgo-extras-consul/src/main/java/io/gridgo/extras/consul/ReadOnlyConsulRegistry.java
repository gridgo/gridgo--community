package io.gridgo.extras.consul;

import com.orbitz.consul.Consul;

import io.gridgo.framework.support.Registry;
import lombok.Getter;
import lombok.NonNull;

public class ReadOnlyConsulRegistry extends ConsulRegistry {

    @Getter
    private Consul client;

    public ReadOnlyConsulRegistry() {
        this.client = Consul.builder().build();
    }

    public ReadOnlyConsulRegistry(String url) {
        this.client = Consul.builder().withUrl(url).build();
    }

    public ReadOnlyConsulRegistry(Consul client) {
        this.client = client;
    }

    @Override
    public Object lookup(String name) {
        return client.keyValueClient().getValueAsString(name).orElse(null);
    }

    @Override
    public Registry register(String name, @NonNull Object answer) {
        return this;
    }
}
