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
        data = Data(mapOf(TaskSpec.DATA_PROJECT_ID to "123"))
        whenever(workerAdapter.inputData).thenReturn(data)
    }

    @Test
    fun `when task returns true should work succeed`() {
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { true })

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.success()))
    }

    @Test
    fun `when task returns false retries if numberOfRetries not specified`() {
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { false })
        whenever(spec.numberOfRetries).thenReturn(TaskSpec.UNLIMITED_NUMBER_OF_RETRIES)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.retry()))
    }

    @Test
    fun `when task returns false retries if numberOfRetries is specified and is lower than runAttemptCount`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(1)
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { false })
        whenever(spec.numberOfRetries).thenReturn(3)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.retry()))
    }

    @Test
    fun `when task returns false fails if numberOfRetries is specified and is equal to runAttemptCount`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(3)
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { false })
        whenever(spec.numberOfRetries).thenReturn(3)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.failure()))
    }

    @Test
    fun `when task returns false fails if numberOfRetries is specified and is higher than runAttemptCount`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(4)
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { false })
        whenever(spec.numberOfRetries).thenReturn(3)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.failure()))
    }

    @Test
    fun `when numberOfRetries is not specified should data contain DATA_LAST_UNIQUE_EXECUTION equal true`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(1)
        whenever(spec.numberOfRetries).thenReturn(TaskSpec.UNLIMITED_NUMBER_OF_RETRIES)
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { true })

        workerAdapter.doWork()

        verify(spec).getTask(
            any(),
            eq(
                mapOf(
                    TaskSpec.DATA_PROJECT_ID to "123",
                    TaskSpec.DATA_LAST_UNIQUE_EXECUTION to "true"
                )
            )
        )
    }

    @Test
    fun `when numberOfRetries is specified and it is higher than runAttemptCount, data contains DATA_LAST_UNIQUE_EXECUTION equal false`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(1)
        whenever(spec.numberOfRetries).thenReturn(3)
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { true })

        workerAdapter.doWork()

        verify(spec).getTask(
            any(),
            eq(
                mapOf(
                    TaskSpec.DATA_PROJECT_ID to "123",
                    TaskSpec.DATA_LAST_UNIQUE_EXECUTION to "false"
                )
            )
        )
    }

    @Test
    fun `when numberOfRetries is specified and it is equal to runAttemptCount, data contains DATA_LAST_UNIQUE_EXECUTION equal true`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(1)
        whenever(spec.numberOfRetries).thenReturn(1)
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { true })

        workerAdapter.doWork()

        verify(spec).getTask(
            any(),
            eq(
                mapOf(
                    TaskSpec.DATA_PROJECT_ID to "123",
                    TaskSpec.DATA_LAST_UNIQUE_EXECUTION to "true"
                )
            )
        )
    }

    private class TestAdapter(spec: TaskSpec, context: Context, workerParams: WorkerParameters) :
        WorkerAdapter(spec, context, workerParams)
}
