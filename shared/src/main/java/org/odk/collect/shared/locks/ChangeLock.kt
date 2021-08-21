package org.odk.collect.shared.locks

import java.util.function.Function

interface ChangeLock {

    /**
     * @param function some work to be executed after attempting to acquire the lock. The function
     * will be passed `true` if the lock was acquired and `false` if not. The
     * function return value will be returned from the call to this method.
     */
    fun <T> withLock(function: Function<Boolean, T>): T
}
