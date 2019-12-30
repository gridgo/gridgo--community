package io.gridgo.extras.concurrent;

public interface ReferenceCounter {

    static ReferenceCounter newBlocking(int initValue) {
        return new BlockingReferenceCounter(initValue);
    }

    void waitFor(int value);

    void increment();

    void decrement();

    void lockIncrementAndWaitFor(int value);

    void lockDecrementAndWaitFor(int value);
}
