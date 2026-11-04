package org.odk.collect.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch

class WorkQueueTest {

    @Test
    fun `#set adds item for consumption`() {
        val workQueue = WorkQueue<Int>()
        workQueue.set { listOf(5) }

        val consumer = mockConsumer()
        workQueue.consume(consumer)
        verify(consumer).invoke(listOf(5))
    }

    @Test
    fun `#set replaces previous items`() {
        val workQueue = WorkQueue<Int>()
        workQueue.set { listOf(5, 6) }
        workQueue.set { listOf(7) }

        val consumer = mockConsumer()
        workQueue.consume(consumer)
        verify(consumer).invoke(listOf(7))
    }

    @Test
    fun `#set returns false if queue is locked`() = runBlocking {
        val workQueue = WorkQueue<Int>()
        val testBarrier = CountDownLatch(1)
        val addJobBarrier = CountDownLatch(1)

        val addJob = CoroutineScope(Dispatchers.IO).launch {
            workQueue.set {
                testBarrier.countDown()
                addJobBarrier.await()
                listOf(1, 2, 3)
            }
        }

        testBarrier.await()
        val result = workQueue.set { listOf(4, 5, 6) }
        assertThat(result, equalTo(false))

        addJobBarrier.countDown()
        addJob.join()

        val consumer = mockConsumer()
        workQueue.consume(consumer)
        verify(consumer).invoke(listOf(1, 2, 3))
    }

    @Test
    fun `#set returns true if queue is not locked`() {
        val workQueue = WorkQueue<Int>()
        val result = workQueue.set { listOf(4, 5, 6) }
        assertThat(result, equalTo(true))
    }

    @Test
    fun `#set calls onClear`() {
        val onClear = mock<(List<Int>) -> Unit>()
        val workQueue = WorkQueue(onClear = onClear)

        workQueue.set { listOf(1, 2, 3) }
        verify(onClear).invoke(emptyList())
        workQueue.set { listOf(4, 5, 6) }
        verify(onClear).invoke(listOf(1, 2, 3))
    }

    @Test
    fun `#consume clears items`() {
        val workQueue = WorkQueue<Int>()
        workQueue.set { listOf(5, 6) }

        val consumer1 = mockConsumer()
        workQueue.consume(consumer1)
        verify(consumer1).invoke(listOf(5, 6))

        val consumer2 = mockConsumer()
        workQueue.consume(consumer2)
        verify(consumer2).invoke(emptyList())
    }

    @Test
    fun `#consume returns false if queue is locked`() = runBlocking {
        val workQueue = WorkQueue<Int>()
        val testBarrier = CountDownLatch(1)
        val addJobBarrier = CountDownLatch(1)

        val addJob = CoroutineScope(Dispatchers.IO).launch {
            workQueue.set {
                testBarrier.countDown()
                addJobBarrier.await()
                listOf(1, 2, 3)
            }
        }

        testBarrier.await()
        val consumer = mockConsumer()
        val result = workQueue.consume { }
        assertThat(result, equalTo(false))

        addJobBarrier.countDown()
        addJob.join()
        verify(consumer, never()).invoke(any())
    }

    @Test
    fun `#consume calls onClear after consumer`() {
        val onClear = mock<(List<Int>) -> Unit>()
        val workQueue = WorkQueue(onClear = onClear)

        workQueue.set { listOf(4, 5, 6) }
        workQueue.consume { verify(onClear, never()).invoke(listOf(4, 5, 6)) }
        verify(onClear).invoke(listOf(4, 5, 6))
    }
}

private fun mockConsumer(): (List<Int>) -> Unit = mock()
