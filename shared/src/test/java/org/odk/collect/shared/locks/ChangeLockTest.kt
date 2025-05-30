package org.odk.collect.shared.locks

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

abstract class ChangeLockTest {
    abstract fun buildSubject(): ChangeLock

    @Test
    fun `tryLock acquires the lock if it is not acquired for default lock id`() {
        val changeLock = buildSubject()

        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(true))
    }

    @Test
    fun `tryLock acquires the lock if it is not acquired for custom lock id`() {
        val changeLock = buildSubject()

        val acquired = changeLock.tryLock("foo")

        assertThat(acquired, equalTo(true))
    }

    @Test
    fun `tryLock does not acquire the lock if it is already acquired for default lock id`() {
        val changeLock = buildSubject()

        changeLock.tryLock()
        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(false))
    }

    @Test
    fun `tryLock does not acquire the lock if it is already acquired for custom lock id`() {
        val changeLock = buildSubject()

        changeLock.tryLock("foo")
        val acquired = changeLock.tryLock("foo")

        assertThat(acquired, equalTo(false))
    }

    @Test
    fun `lock acquires the lock if it is not acquired for default lock id`() {
        val changeLock = buildSubject()

        changeLock.lock()
        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(false))
    }

    @Test
    fun `lock acquires the lock if it is not acquired for custom lock id`() {
        val changeLock = buildSubject()

        changeLock.lock("foo")
        val acquired = changeLock.tryLock("foo")

        assertThat(acquired, equalTo(false))
    }

    @Test(expected = IllegalStateException::class)
    fun `lock throws an exception if the lock is already acquired for default lock id`() {
        val changeLock = buildSubject()

        changeLock.lock()
        changeLock.lock()
    }

    @Test(expected = IllegalStateException::class)
    fun `lock throws an exception if the lock is already acquired for custom lock id`() {
        val changeLock = buildSubject()

        changeLock.lock("foo")
        changeLock.lock("foo")
    }

    @Test
    fun `unlock releases the lock for default lock ids`() {
        val changeLock = buildSubject()

        changeLock.tryLock()
        changeLock.unlock()
        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(true))
    }

    @Test
    fun `unlock releases the lock for matching custom lock ids`() {
        val changeLock = buildSubject()

        changeLock.tryLock("foo")
        changeLock.unlock("foo")
        val acquired = changeLock.tryLock("foo")

        assertThat(acquired, equalTo(true))
    }

    @Test
    fun `unlock does not release the lock for not matching custom lock ids`() {
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

        changeLock.tryLock()
        changeLock.withLock { acquired ->
            assertThat(acquired, equalTo(false))
        }
    }

    @Test
    fun `withLock releases the lock after performing the job`() {
        val changeLock = buildSubject()

        changeLock.withLock {}

        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(true))
    }

    @Test
    fun `withLock does not release the lock it it was not able to acquire it`() {
        val changeLock = buildSubject()

        changeLock.tryLock()
        changeLock.withLock {}

        val acquired = changeLock.tryLock()

        assertThat(acquired, equalTo(false))
    }
}
