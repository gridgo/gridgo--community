package io.gridgo.extras.concurrent;

import io.gridgo.extras.concurrent.BlockingReferenceCounter.ReferenceCounterBuilder;

public interface ReferenceCounter {

    static ReferenceCounterBuilder builder() {
        return BlockingReferenceCounter.builder();
    }

    /**
     * wait for counter to reach value
     * 
     * @param value value to wait
     */
    void waitFor(int value);

    /**
     * increment this counter
     * @return TODO
     */
    boolean increment();

    /**
     * decrement this counter
     * @return TODO
     */
    boolean decrement();

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
