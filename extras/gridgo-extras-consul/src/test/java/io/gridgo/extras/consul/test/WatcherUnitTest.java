package io.gridgo.extras.consul.test;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import io.gridgo.extras.consul.PrefixedConsulRegistry;

public class WatcherUnitTest {

    @Test
    public void testWatch() throws InterruptedException {
        var latch = new CountDownLatch(2);
        var registry = new PrefixedConsulRegistry("io.gridgo.test");
        var disposable = registry.watchForChange("test/key1", event -> {
            System.out.println(event.getChangedKey() + ": " + event.getNewValue());
            latch.countDown();
        });

        var registry1 = new PrefixedConsulRegistry("io.gridgo.test");
        registry1.register("test/key1", "hello1");
        
        latch.await();
        disposable.dispose();
    }
}
