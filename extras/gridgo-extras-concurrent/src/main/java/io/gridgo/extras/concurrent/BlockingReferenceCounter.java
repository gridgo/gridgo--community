package io.gridgo.extras.concurrent;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

class BlockingReferenceCounter implements ReferenceCounter {

    private final AtomicInteger counter;

    private final AtomicReference<CountDownLatch> incrementLock = new AtomicReference<>();

    private final AtomicReference<CountDownLatch> decrementLock = new AtomicReference<>();

    private final Map<Integer, CountDownLatch> waiting = new NonBlockingHashMap<>();

    private final Lock _lock = new ReentrantLock();

    BlockingReferenceCounter(int initValue) {
        this.counter = new AtomicInteger(initValue);
    }

    BlockingReferenceCounter() {
        this(0);
    }

    private <V> V accessCounter(Callable<V> runner) {
        var hasLock = false;
        try {
            _lock.lockInterruptibly();
            hasLock = true;
            return runner.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (hasLock)
                _lock.unlock();
        }
    }

    @Override
    public void waitFor(int value) {
        var latch = accessCounter(() -> {
            if (counter.get() == value)
                return null;
            return getWaitingLatch(value);
        });

        if (latch == null)
            return;

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

    private void change(int delta, AtomicReference<CountDownLatch> lockHolder) {
        while (true) {
            var theLock = accessCounter(() -> {
                var lock = lockHolder.get();
                if (lock == null)
                    triggerValue(counter.addAndGet(delta));
                return lock;
            });

            if (theLock == null)
                return;

            try {
                theLock.await();
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

    private void doNothing() {
        // do nothing
    }

    private Unlockable lockAndWaitFor(int value, AtomicReference<CountDownLatch> lockHolder) {
        while (true) {
            var lock = new CountDownLatch(1);
            Boolean success = accessCounter(() -> {
                if (counter.get() == value) {
                    triggerValue(value);
                    return null;
                }
                return lockHolder.compareAndSet(null, lock);
            });

            if (success == null)
                return this::doNothing;

            try {
                if (success) {
                    getWaitingLatch(value).await();
                    return () -> {
                        lock.countDown();
                        if (!lockHolder.compareAndSet(lock, null))
                            throw new IllegalStateException("Cannot unlock");
                    };
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
    public Unlockable lockIncrementAndWaitFor(int value) {
        return lockAndWaitFor(value, incrementLock);
    }

    @Override
    public Unlockable lockDecrementAndWaitFor(int value) {
        return lockAndWaitFor(value, decrementLock);
    }
}
