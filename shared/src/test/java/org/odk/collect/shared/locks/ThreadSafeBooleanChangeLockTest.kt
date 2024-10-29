package org.odk.collect.shared.locks

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ThreadSafeBooleanChangeLockTest {
    private val changeLock = ThreadSafeBooleanChangeLock()

    @Test
    fun `tryLock acquires the lock if it is not acquired`() {
        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(true))
    }

    @Test
    fun `tryLock does not acquire the lock if it is already acquired`() {
        changeLock.tryLock()
        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(false))
    }

    @Test
    fun `lock acquires the lock if it is not acquired`() {
        changeLock.lock()
        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(false))
    }

    @Test(expected = IllegalStateException::class)
    fun `lock throws an exception if the lock is already acquired`() {
        changeLock.lock()
        changeLock.lock()
    }

    @Test
    fun `unlock releases the lock`() {
        changeLock.tryLock()
        changeLock.unlock()
        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(true))
    }

    @Test
    fun `withLock acquires the lock if it is not acquired`() {
        changeLock.withLock { acquired ->
            assertThat(acquired, equalTo(true))
        }
    }

    @Test
    fun `withLock does not acquire the lock if it is already acquired`() {
        changeLock.tryLock()
        changeLock.withLock { acquired ->
            assertThat(acquired, equalTo(false))
        }
    }

    @Test
    fun `withLock releases the lock after performing the job`() {
        changeLock.withLock {}

        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(true))
    }

    @Test
    fun `withLock does not release the lock it it was not able to acquire it`() {
        changeLock.tryLock()
        changeLock.withLock {}

        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(false))
    }
}
