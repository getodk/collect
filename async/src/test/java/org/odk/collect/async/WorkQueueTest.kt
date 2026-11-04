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
    fun `#add adds item for consumption`() {
        val workQueue = WorkQueue<Int>()
        workQueue.add { listOf(5) }

        val consumer = mockConsumer()
        workQueue.consume(consumer)
        verify(consumer).invoke(listOf(5))
    }

    @Test
    fun `#add adds items in order`() {
        val workQueue = WorkQueue<Int>()
        workQueue.add { listOf(5, 6) }
        workQueue.add { listOf(7) }

        val consumer = mockConsumer()
        workQueue.consume(consumer)
        verify(consumer).invoke(listOf(5, 6, 7))
    }

    @Test
    fun `#add returns false if queue is locked`() = runBlocking {
        val workQueue = WorkQueue<Int>()
        val testBarrier = CountDownLatch(1)
        val addJobBarrier = CountDownLatch(1)

        val addJob = CoroutineScope(Dispatchers.IO).launch {
            workQueue.add {
                testBarrier.countDown()
                addJobBarrier.await()
                listOf(1, 2, 3)
            }
        }

        testBarrier.await()
        val result = workQueue.add { listOf(4, 5, 6) }
        assertThat(result, equalTo(false))

        addJobBarrier.countDown()
        addJob.join()

        val consumer = mockConsumer()
        workQueue.consume(consumer)
        verify(consumer).invoke(listOf(1, 2, 3))
    }

    @Test
    fun `#add returns true if queue is not locked`() {
        val workQueue = WorkQueue<Int>()
        val result = workQueue.add { listOf(4, 5, 6) }
        assertThat(result, equalTo(true))
    }

    @Test
    fun `#consume clears items`() {
        val workQueue = WorkQueue<Int>()
        workQueue.add { listOf(5, 6) }

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
            workQueue.add {
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
}

private fun mockConsumer(): (List<Int>) -> Unit = mock()
