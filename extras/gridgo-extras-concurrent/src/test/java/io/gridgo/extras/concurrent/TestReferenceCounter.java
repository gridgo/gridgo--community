package io.gridgo.extras.concurrent;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import io.gridgo.utils.ThreadUtils;

public class TestReferenceCounter {

    private static String TEST_TEXT = "this is test text";

    private void run(Runnable runner) {
        new Thread(runner).start();
    }

    @Test
    public void testWaitFor() {
        var val = new AtomicReference<String>(null);
        var counter = ReferenceCounter.newBlocking(1);

        run(() -> {
            ThreadUtils.sleep(5);
            val.set(TEST_TEXT);
            counter.decrement();
        });

        counter.waitFor(0);
        assertEquals(TEST_TEXT, val.get());
    }

    @Test
    public void testLockIncrement() {
        var ref1 = new AtomicReference<String>(null);
        var ref2 = new AtomicReference<String>(null);

        var counter = ReferenceCounter.newBlocking(1);

        run(() -> {
            ThreadUtils.sleep(5);
            ref1.set(TEST_TEXT);
            counter.decrement();
        });

        run(() -> {
            ThreadUtils.sleep(5);
            counter.increment();
            ref2.set(ref1.get());
        });

        counter.lockIncrementAndWaitFor(0);

        ThreadUtils.sleep(10);

        assertEquals(TEST_TEXT, ref1.get());
        assertEquals(TEST_TEXT, ref2.get());
    }

    @Test
    public void testLockDecrement() {
        var ref1 = new AtomicReference<String>(null);
        var ref2 = new AtomicReference<String>(null);

        var counter = ReferenceCounter.newBlocking(0);

        run(() -> {
            ThreadUtils.sleep(5);
            ref1.set(TEST_TEXT);
            counter.increment();
        });

        run(() -> {
            ThreadUtils.sleep(5);
            counter.decrement();
            ref2.set(ref1.get());
        });

        counter.lockDecrementAndWaitFor(1);

        ThreadUtils.sleep(100);

        assertEquals(TEST_TEXT, ref1.get());
        assertEquals(TEST_TEXT, ref2.get());
    }

    @Test
    public void testLockOnCurrentValue() {
        var counter = ReferenceCounter.newBlocking(0);
        counter.lockDecrementAndWaitFor(0);
    }

    @Test
    public void testWaitOnCurrentValue() {
        var counter = ReferenceCounter.newBlocking(0);
        counter.waitFor(0);
    }
}
