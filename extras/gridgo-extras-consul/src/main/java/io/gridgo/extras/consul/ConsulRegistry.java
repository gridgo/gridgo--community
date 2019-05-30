package io.gridgo.extras.consul;

import com.orbitz.consul.Consul;

import io.gridgo.framework.support.Registry;
import lombok.NonNull;

public class ConsulRegistry implements Registry {

    private Consul client;

    public ConsulRegistry() {
        this.client = Consul.builder().build();
    }

    public ConsulRegistry(String url) {
        this.client = Consul.builder().withUrl(url).build();
    }

    public ConsulRegistry(Consul client) {
        this.client = client;
    }

    @Override
    public Object lookup(String name) {
        return client.keyValueClient().getValueAsString(name);
    }

    @Override
    public Registry register(String name, @NonNull Object answer) {
        var convertedAnswer = convertValue(answer);
        if (convertValue(answer) != null) {
            client.keyValueClient().putValue(name, convertedAnswer);
        }
        return this;
    }

    private String convertValue(Object answer) {
        if (answer == null)
            return null;
        if (answer instanceof byte[])
            return new String((byte[]) answer);
        if (answer instanceof String)
            return (String) answer;
        return null;
    }
}
