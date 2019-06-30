package io.gridgo.extras.consul;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.orbitz.consul.Consul;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;

import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.watch.impl.AbstractWatchable;
import io.gridgo.framework.support.watch.impl.DefaultWatchEvent;

public abstract class ConsulRegistry extends AbstractWatchable implements Registry {

    private List<KVCache> keyCache = new CopyOnWriteArrayList<>();

    @Override
    protected void onRegisterWatch(String key) {
        var client = getClient().keyValueClient();
        var cache = KVCache.newCache(client, key);
        cache.addListener(newValues -> {
            newValues.forEach((k, v) -> {
                fireChange(key, mapEvent(v));
            });
        });
        cache.start();
        keyCache.add(cache);
    }

    protected DefaultWatchEvent mapEvent(Value v) {
        return new DefaultWatchEvent(v.getKey(), v.getValueAsString().orElse(null));
    }

    protected abstract Consul getClient();

    public void stop() {
        for (var cache : keyCache)
            cache.stop();
    }
}
