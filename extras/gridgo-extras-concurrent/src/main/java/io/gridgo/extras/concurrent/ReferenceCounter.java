package io.gridgo.extras.concurrent;

import io.gridgo.extras.concurrent.BlockingReferenceCounter.ReferenceCounterBuilder;

public interface ReferenceCounter {

    static ReferenceCounterBuilder builder() {
        return BlockingReferenceCounter.builder();
    }

    /**
     * get counter value
     * 
     * @return current counter value
     */
    int count();

    /**
     * wait for counter to reach value
     * 
     * @param value value to wait
     */
    void waitFor(int value);

    /**
     * increment this counter
     * 
     * @return true if value incremented successful, false otherwise
     */
    boolean increment();

    /**
     * check if increment is being blocked
     * 
     * @return
     */
    boolean isIncrementBlocked();

    /**
     * decrement this counter
     * 
     * @return true if value decremented successful, false otherwise
     */
    boolean decrement();

    /**
     * check if decrement is being blocked
     * 
     * @return
     */
    boolean isDecrementBlocked();

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
