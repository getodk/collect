package org.odk.collect.async

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
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
import java.util.function.Supplier

@RunWith(AndroidJUnit4::class)
class WorkerAdapterTest {
    private lateinit var workerAdapter: WorkerAdapter
    private val spec = mock<TaskSpec>()
    private val workerParameters = mock<WorkerParameters>()
    private lateinit var data: Data

    @Before
    fun setup() {
        workerAdapter =
            TestAdapter(spec, ApplicationProvider.getApplicationContext(), workerParameters)
        data = Data(emptyMap<String, String>())
        whenever(workerAdapter.inputData).thenReturn(data)
    }

    @Test
    fun `when task returns true should work succeed`() {
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { true })

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.success()))
    }

    @Test
    fun `when task returns false retries if maxRetries not specified`() {
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { false })
        whenever(spec.maxRetries).thenReturn(TaskSpec.UNLIMITED_NUMBER_OF_RETRIES)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.retry()))
    }

    @Test
    fun `when task returns false retries if maxRetries is specified and is lower than runAttemptCount`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(1)
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { false })
        whenever(spec.maxRetries).thenReturn(3)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.retry()))
    }

    @Test
    fun `when task returns false fails if maxRetries is specified and is equal to runAttemptCount`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(3)
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { false })
        whenever(spec.maxRetries).thenReturn(3)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.failure()))
    }

    @Test
    fun `when task returns false fails if maxRetries is specified and is higher than runAttemptCount`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(4)
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { false })
        whenever(spec.maxRetries).thenReturn(3)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.failure()))
    }

    @Test
    fun `when maxRetries is not specified, task called with isLastUniqueExecution true`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(1)
        whenever(spec.maxRetries).thenReturn(TaskSpec.UNLIMITED_NUMBER_OF_RETRIES)
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { true })

        workerAdapter.doWork()

        verify(spec).getTask(any(), any(), eq(true))
    }

    @Test
    fun `when maxRetries is specified and it is higher than runAttemptCount, task called with isLastUniqueExecution false`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(1)
        whenever(spec.maxRetries).thenReturn(3)
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { true })

        workerAdapter.doWork()

        verify(spec).getTask(any(), any(), eq(false))
    }

    @Test
    fun `when maxRetries is specified and it is equal to runAttemptCount, task called with isLastUniqueExecution false`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(1)
        whenever(spec.maxRetries).thenReturn(1)
        whenever(spec.getTask(any(), any(), any())).thenReturn(Supplier { true })

        workerAdapter.doWork()

        verify(spec).getTask(any(), any(), eq(true))
    }

    private class TestAdapter(spec: TaskSpec, context: Context, workerParams: WorkerParameters) :
        WorkerAdapter(spec, context, workerParams)
}
