package io.gridgo.extras.consul;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.orbitz.consul.Consul;
import com.orbitz.consul.model.kv.Value;

import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.watch.Disposable;
import io.gridgo.framework.support.watch.WatchEvent;
import io.gridgo.framework.support.watch.impl.DefaultWatchEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrefixedConsulRegistry extends ConsulRegistry {

    private static final String DEFAULT_PREFIX = "";

    private String prefix;

    @Getter
    private Consul client;

    public PrefixedConsulRegistry() {
        this(DEFAULT_PREFIX);
    }

    public PrefixedConsulRegistry(String prefix) {
        this(prefix, Consul.builder().build());
    }

    public PrefixedConsulRegistry(String prefix, String url) {
        this(prefix, Consul.builder().withUrl(url).build());
    }

    public PrefixedConsulRegistry(String prefix, Consul client) {
        this.prefix = prefix;
        this.client = client;
        if (prefix == DEFAULT_PREFIX)
            log.warn("Using default prefix for consul is not recommended");
    }

    @Override
    public Disposable watchForChange(String key, Consumer<WatchEvent> handler) {
        return super.watchForChange(normalize(key), handler);
    }

    @Override
    protected DefaultWatchEvent mapEvent(Value v) {
        return new DefaultWatchEvent(v.getKey().substring(prefix.length()), v.getValueAsString().orElse(null));
    }

    @Override
    public Object lookup(String name) {
        name = normalize(name);
        if (name.endsWith("/")) {
            return convertList(name);
        }
        return client.keyValueClient().getValueAsString(name).orElse(null);
    }

    protected String normalize(String name) {
        return prefix + "/" + name;
    }

    protected Object convertList(String name) {
        var values = client.keyValueClient().getValues(name);
        var map = values.stream().collect(
                Collectors.toMap(v -> v.getKey().substring(name.length()), v -> v.getValueAsString().orElse(null)));
        return BObject.of(map);
    }

    @Override
    public Registry register(String name, @NonNull Object answer) {
        name = normalize(name);
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
