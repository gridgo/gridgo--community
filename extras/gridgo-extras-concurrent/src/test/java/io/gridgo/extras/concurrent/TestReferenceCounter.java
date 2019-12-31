package io.gridgo.extras.concurrent;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import io.gridgo.utils.ThreadUtils;

public class TestReferenceCounter {

    private static final String TEST_TEXT_1 = "this is test text 1";
    private static final String TEST_TEXT = "this is test text";

    private void run(Runnable runner) {
        new Thread(runner).start();
    }

    @Test
    public void testWaitFor() {
        var val = new AtomicReference<String>(null);
        var counter = ReferenceCounter.builder().initValue(1).build();

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

        var counter = ReferenceCounter.builder().initValue(1).build();

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

        var unlockable = counter.lockIncrementAndWaitFor(0);
        assertEquals(TEST_TEXT, ref1.get());

        ref1.set(TEST_TEXT_1);
        unlockable.unlock();

        ThreadUtils.sleep(10);
        assertEquals(TEST_TEXT_1, ref2.get());
    }

    @Test
    public void testLockDecrement() {
        var ref1 = new AtomicReference<String>(null);
        var ref2 = new AtomicReference<String>(null);

        var counter = ReferenceCounter.builder().initValue(0).build();

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

        var unlockable = counter.lockDecrementAndWaitFor(1);
        assertEquals(TEST_TEXT, ref1.get());

        ref1.set(TEST_TEXT_1);
        unlockable.unlock();

        ThreadUtils.sleep(10);

        assertEquals(TEST_TEXT_1, ref2.get());
    }

    @Test
    public void testLockOnCurrentValue() {
        var counter = ReferenceCounter.builder().initValue(0).build();
        counter.lockDecrementAndWaitFor(0);
    }

    @Test
    public void testWaitOnCurrentValue() {
        var counter = ReferenceCounter.builder().initValue(0).build();
        counter.waitFor(0);
    }
}
