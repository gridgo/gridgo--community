package io.gridgo.extras.etcd.test;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.extras.etcd.EtcdRegistry;

public class EtcdRegistryUnitTest {

    @Test
    public void testGetAndSetString() {
        var registry = new EtcdRegistry();
        registry.register("test", "hello");
        Assert.assertEquals("hello", registry.lookup("test", String.class));
    }

    @Test
    public void testGetAndSetObject() {
        var registry = new EtcdRegistry();
        registry.register("test", BValue.of("test"));
        Assert.assertEquals("(STRING = test)", registry.lookup("test", String.class));
    }
}
