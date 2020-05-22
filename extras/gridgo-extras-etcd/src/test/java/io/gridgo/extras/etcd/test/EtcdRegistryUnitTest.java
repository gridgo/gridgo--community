package io.gridgo.extras.etcd.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.extras.etcd.EtcdRegistry;

@Ignore
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
        registry.register("test", BValue.of("hello").toString());
        Assert.assertEquals("(STRING = hello)", registry.lookup("test", String.class));
    }
    
    @Test
    public void testGetAndSetRaw() {
        var registry = new EtcdRegistry();
        registry.register("test", BObject.of("test", "hello"));
        var answer = registry.lookup("test", BObject.class);
        Assert.assertEquals("hello", answer.getString("test"));
    }
}
