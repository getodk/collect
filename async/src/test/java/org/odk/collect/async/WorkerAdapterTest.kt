package org.odk.collect.async

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.testing.TestWorkerBuilder
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Executors
import java.util.function.Supplier

@RunWith(AndroidJUnit4::class)
class WorkerAdapterTest {
    private lateinit var worker: Worker
    companion object {
        private lateinit var spec: TaskSpec
    }

    @Before
    fun setup() {
        spec = mock()
        worker = TestWorkerBuilder<TestWorker>(
            context = ApplicationProvider.getApplicationContext(),
            executor = Executors.newSingleThreadExecutor(),
            runAttemptCount = 0 // without setting this explicitly attempts in tests are counted starting from 1 instead of 0 like in production code
        ).build()
    }

    @Test
    fun `when task returns true should work succeed`() {
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { true })

        assertThat(worker.doWork(), `is`(ListenableWorker.Result.success()))
    }

    @Test
    fun `when task returns false, retries if maxRetries not specified`() {
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { false })
        whenever(spec.maxRetries).thenReturn(null)

        assertThat(worker.doWork(), `is`(ListenableWorker.Result.retry()))
    }

    @Test
    fun `when task returns false, retries if maxRetries is specified and is higher than runAttemptCount`() {
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { false })
        whenever(spec.maxRetries).thenReturn(3)

        assertThat(worker.doWork(), `is`(ListenableWorker.Result.retry()))
    }

    @Test
    fun `when task returns false, fails if maxRetries is specified and is equal to runAttemptCount`() {
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { false })
        whenever(spec.maxRetries).thenReturn(0)

        assertThat(worker.doWork(), `is`(ListenableWorker.Result.failure()))
    }

    @Test
    fun `when task returns false, fails if maxRetries is specified and is lower than runAttemptCount`() {
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { false })
        whenever(spec.maxRetries).thenReturn(0)

        assertThat(worker.doWork(), `is`(ListenableWorker.Result.failure()))
    }

    @Test
    fun `when maxRetries is not specified, task called with isLastUniqueExecution true`() {
        whenever(spec.maxRetries).thenReturn(null)
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { true })

        worker.doWork()

        verify(spec).getTask(any(), any(), eq(true))
    }

    @Test
    fun `when maxRetries is specified and it is higher than runAttemptCount, task called with isLastUniqueExecution false`() {
        whenever(spec.maxRetries).thenReturn(3)
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { true })

        worker.doWork()

        verify(spec).getTask(any(), any(), eq(false))
    }

    @Test
    fun `when maxRetries is specified and it is equal to runAttemptCount, task called with isLastUniqueExecution true`() {
        whenever(spec.maxRetries).thenReturn(0)
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { true })

        worker.doWork()

        verify(spec).getTask(any(), any(), eq(true))
    }

    @Test
    fun `when maxRetries is specified and it is lower than runAttemptCount, task called with isLastUniqueExecution true`() {
        whenever(spec.maxRetries).thenReturn(0)
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { true })

        worker.doWork()

        verify(spec).getTask(any(), any(), eq(true))
    }

    class TestWorker(context: Context, parameters: WorkerParameters) : WorkerAdapter(spec, context, parameters)
}
