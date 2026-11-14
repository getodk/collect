package org.odk.collect.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch

class WorkQueueTest {

    @Test
    fun `#set adds item for consumption`() {
        val workQueue = WorkQueue<Int>()
        workQueue.set {
            replace(listOf(5))
        }

        val consumer = mockConsumer()
        workQueue.consume(consumer)
        verify(consumer).invoke(listOf(5))
    }

    @Test
    fun `#set replaces previous items`() {
        val workQueue = WorkQueue<Int>()
        workQueue.set {
            replace(listOf(5, 6))
        }
        workQueue.set {
            replace(listOf(7))
        }

        val consumer = mockConsumer()
        workQueue.consume(consumer)
        verify(consumer).invoke(listOf(7))
    }

    @Test
    fun `#set does nothing queue is locked`() = runBlocking {
        val workQueue = WorkQueue<Int>()
        val testBarrier = CountDownLatch(1)
        val addJobBarrier = CountDownLatch(1)

        val addJob = CoroutineScope(Dispatchers.IO).launch {
            workQueue.set {
                testBarrier.countDown()
                addJobBarrier.await()
                replace(listOf(1, 2, 3))
            }
        }

        testBarrier.await()
        workQueue.set {
            replace(listOf(4, 5, 6))
        }

        addJobBarrier.countDown()
        addJob.join()

        val consumer = mockConsumer()
        workQueue.consume(consumer)
        verify(consumer).invoke(listOf(1, 2, 3))
    }

    @Test
    fun `#set calls onClear`() {
        val onClear = mock<(List<Int>) -> Unit>()
        val workQueue = WorkQueue(onClear = onClear)

        workQueue.set {
            replace(listOf(1, 2, 3))
        }
        verify(onClear).invoke(emptyList())

        workQueue.set {
            replace(listOf(4, 5, 6))
        }
        verify(onClear).invoke(listOf(1, 2, 3))
    }

    @Test
    fun `#set exposes lock value`() = runBlocking {
        val workQueue = WorkQueue<Int>()
        val testBarrier = CountDownLatch(1)
        val addJobBarrier = CountDownLatch(1)

        val addJob = CoroutineScope(Dispatchers.IO).launch {
            workQueue.set {
                testBarrier.countDown()
                addJobBarrier.await()
            }
        }

        testBarrier.await()
        val lockedResult = workQueue.set { hasLock }
        assertThat(lockedResult, equalTo(false))

        addJobBarrier.countDown()
        addJob.join()

        val unlockedResult = workQueue.set { hasLock }
        assertThat(unlockedResult, equalTo(true))
    }

    @Test
    fun `#consume clears items`() {
        val workQueue = WorkQueue<Int>()
        workQueue.set {
            replace(listOf(5, 6))
        }

        val consumer1 = mockConsumer()
        workQueue.consume(consumer1)
        verify(consumer1).invoke(listOf(5, 6))

        val consumer2 = mockConsumer()
        workQueue.consume(consumer2)
        verify(consumer2).invoke(emptyList())
    }

    @Test
    fun `#consume calls onClear after consumer`() {
        val onClear = mock<(List<Int>) -> Unit>()
        val workQueue = WorkQueue(onClear = onClear)

        workQueue.set {
            replace(listOf(4, 5, 6))
        }
        workQueue.consume { verify(onClear, never()).invoke(listOf(4, 5, 6)) }
        verify(onClear).invoke(listOf(4, 5, 6))
    }
}

private fun mockConsumer(): (List<Int>) -> Unit = mock()
