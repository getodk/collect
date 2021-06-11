package org.odk.collect.testshared;

import org.odk.collect.shared.locks.ChangeLock;

import java.util.function.Function;

public class BooleanChangeLock implements ChangeLock {

    private boolean locked;

    @Override
    public <T> T withLock(Function<Boolean, T> function) {
        return function.apply(!locked);
    }

    public void lock() {
        locked = true;
    }
}
