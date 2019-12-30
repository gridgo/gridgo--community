package io.gridgo.extras.concurrent;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

class BlockingReferenceCounter implements ReferenceCounter {

    private final AtomicInteger counter;

    private final AtomicReference<CountDownLatch> incrementLock = new AtomicReference<>();

    private final AtomicReference<CountDownLatch> decrementLock = new AtomicReference<>();

    private final Map<Integer, CountDownLatch> waiting = new NonBlockingHashMap<>();

    BlockingReferenceCounter(int initValue) {
        this.counter = new AtomicInteger(initValue);
    }

    BlockingReferenceCounter() {
        this(0);
    }

    @Override
    public void waitFor(int value) {
        var latch = getWaitingLatch(value);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private CountDownLatch getWaitingLatch(int value) {
        return waiting.compute(value, (k, v) -> v != null ? v : new CountDownLatch(1));
    }

    private void triggerValue(int value) {
        var latch = waiting.remove(value);
        if (latch != null)
            latch.countDown();
    }

    private void change(int delta, AtomicReference<CountDownLatch> latchHolder) {
        while (true) {
            CountDownLatch latch = null;
            synchronized (counter) {
                latch = latchHolder.get();
                if (latch == null) {
                    triggerValue(counter.addAndGet(delta));
                    return;
                }
            }

            if (latch != null)
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
        }
    }

    @Override
    public void increment() {
        change(1, incrementLock);
    }

    @Override
    public void decrement() {
        change(-1, decrementLock);
    }

    private void lockAndWaitFor(int value, AtomicReference<CountDownLatch> lockHolder) {
        while (true) {
            var latch = getWaitingLatch(value);
            var success = false;
            synchronized (counter) {
                success = lockHolder.compareAndSet(null, latch);
            }

            try {
                if (success) {
                    latch.await();
                    if (!lockHolder.compareAndSet(latch, null))
                        throw new IllegalStateException("Cannot unlock");
                    return;
                }

                var currLatch = lockHolder.get();
                if (currLatch != null)
                    currLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void lockIncrementAndWaitFor(int value) {
        lockAndWaitFor(value, incrementLock);
    }

    @Override
    public void lockDecrementAndWaitFor(int value) {
        lockAndWaitFor(value, decrementLock);
    }
}
