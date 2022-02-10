package org.odk.collect.async

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.function.Supplier

@RunWith(AndroidJUnit4::class)
class WorkerAdapterTest {
    private lateinit var workerAdapter: WorkerAdapter
    private val spec = mock<TaskSpec>()
    private val workerParameters = mock<WorkerParameters>()

    @Before
    fun setup() {
        workerAdapter = TestAdapter(spec, ApplicationProvider.getApplicationContext(), workerParameters)
        whenever(workerAdapter.inputData).thenReturn(mock())
    }

    @Test
    fun `when task returns true should work succeed`() {
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { true })

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.success()))
    }

    @Test
    fun `when task returns false should work retry if numberOfRetries not specified`() {
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { false })
        whenever(spec.numberOfRetries).thenReturn(-1)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.retry()))
    }

    @Test
    fun `when task returns false should work retry if numberOfRetries is specified and is lower than runAttemptCount`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(1)
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { false })
        whenever(spec.numberOfRetries).thenReturn(3)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.retry()))
    }

    @Test
    fun `when task returns false should work retry if numberOfRetries is specified and is equal to runAttemptCount`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(3)
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { false })
        whenever(spec.numberOfRetries).thenReturn(3)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.retry()))
    }

    @Test
    fun `when task returns false should work fail if numberOfRetries is specified and is higher than runAttemptCount`() {
        whenever(workerAdapter.runAttemptCount).thenReturn(4)
        whenever(spec.getTask(any(), any())).thenReturn(Supplier { false })
        whenever(spec.numberOfRetries).thenReturn(3)

        assertThat(workerAdapter.doWork(), `is`(ListenableWorker.Result.failure()))
    }

    private class TestAdapter(spec: TaskSpec, context: Context, workerParams: WorkerParameters) :
        WorkerAdapter(spec, context, workerParams)
}
