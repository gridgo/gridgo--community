package io.gridgo.extras.concurrent;

public interface ReferenceCounter {

    static ReferenceCounter newBlocking(int initValue) {
        return new BlockingReferenceCounter(initValue);
    }

    /**
     * wait for counter to reach value
     * 
     * @param value value to wait
     */
    void waitFor(int value);

    /**
     * increment this counter
     */
    void increment();

    /**
     * decrement this counter
     */
    void decrement();

    /**
     * Lock incrementing and stop current thread. Wait until counter reach value.
     * 
     * @param value
     * @return unlockable instead to unlock the counter
     */
    Unlockable lockIncrementAndWaitFor(int value);

    /**
     * Unlock incrementing and stop current thread. Wait until counter reach value.
     * 
     * @param value
     * @return unlockable instead to unlock the counter
     */
    Unlockable lockDecrementAndWaitFor(int value);
}
