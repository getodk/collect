package org.odk.collect.shared.locks

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

abstract class ChangeLockTest {
    abstract fun buildSubject(): ChangeLock

    @Test
    fun `tryLock acquires the lock if it is not acquired`() {
        val changeLock = buildSubject()

        val acquired = changeLock.tryLock("foo")

        assertThat(acquired, equalTo(true))
    }

    @Test
    fun `tryLock does not acquire the lock if it is already acquired`() {
        val changeLock = buildSubject()

        changeLock.tryLock("foo")
        val acquired = changeLock.tryLock("foo")

        assertThat(acquired, equalTo(false))
    }

    @Test
    fun `unlock releases the lock for matching tokens`() {
        val changeLock = buildSubject()

        changeLock.tryLock("foo")
        changeLock.unlock("foo")
        val acquired = changeLock.tryLock("foo")

        assertThat(acquired, equalTo(true))
    }

    @Test
    fun `unlock does not release the lock for not matching tokens`() {
        val changeLock = buildSubject()

        changeLock.tryLock("foo")
        changeLock.unlock("bar")
        val acquired = changeLock.tryLock("foo")

        assertThat(acquired, equalTo(false))
    }

    @Test
    fun `withLock acquires the lock if it is not acquired`() {
        val changeLock = buildSubject()

        changeLock.withLock { acquired ->
            assertThat(acquired, equalTo(true))
        }
    }

    @Test
    fun `withLock does not acquire the lock if it is already acquired`() {
        val changeLock = buildSubject()

        changeLock.tryLock("foo")
        changeLock.withLock { acquired ->
            assertThat(acquired, equalTo(false))
        }
    }

    @Test
    fun `withLock releases the lock after performing the job`() {
        val changeLock = buildSubject()

        changeLock.withLock {}

        val acquired = changeLock.tryLock("foo")

        assertThat(acquired, equalTo(true))
    }

    @Test
    fun `withLock does not release the lock it it was not able to acquire it`() {
        val changeLock = buildSubject()

        changeLock.tryLock("foo")
        changeLock.withLock {}

        val acquired = changeLock.tryLock("foo")

        assertThat(acquired, equalTo(false))
    }
}
