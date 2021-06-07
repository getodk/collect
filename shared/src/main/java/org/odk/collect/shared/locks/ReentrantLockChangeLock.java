package org.odk.collect.shared.locks;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class ReentrantLockChangeLock implements ChangeLock {

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public <T> T withLock(Function<Boolean, T> function) {
        try {
            return function.apply(lock.tryLock());
        } finally {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException ignored) {
                // Ignored
            }
        }
    }
}
