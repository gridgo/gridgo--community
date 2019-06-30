package io.gridgo.extras.consul.test;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.extras.consul.PrefixedConsulRegistry;
import io.gridgo.extras.consul.ReadOnlyConsulRegistry;

public class ConsulRegistryUnitTest {

    @Test
    public void testRegistry() {
        var registry = new PrefixedConsulRegistry("io.gridgo.test");
        registry.register("key", "hello");
        registry.register("test/sub1/key", "hello");
        Assert.assertEquals("hello", registry.lookup("key"));
        Assert.assertEquals("hello", registry.lookup("/key"));
        Assert.assertEquals("hello", registry.lookup("test/sub1/key"));
        Assert.assertEquals("hello", registry.lookup("test/sub1/", BObject.class).getString("key"));

        var readonlyRegistry = new ReadOnlyConsulRegistry();
        Assert.assertEquals("hello", readonlyRegistry.lookup("io.gridgo.test/test/sub1/key"));
    }
}
