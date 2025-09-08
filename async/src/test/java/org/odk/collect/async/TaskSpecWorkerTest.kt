package org.odk.collect.async

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.testing.TestWorkerBuilder
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors
import java.util.function.Supplier

@RunWith(AndroidJUnit4::class)
class TaskSpecWorkerTest {
    private lateinit var worker: Worker

    @Before
    fun setup() {
        worker = TestWorkerBuilder<TaskSpecWorker>(
            context = ApplicationProvider.getApplicationContext(),
            executor = Executors.newSingleThreadExecutor(),
            inputData = Data.Builder()
                .putString(TaskSpecWorker.DATA_TASK_SPEC_CLASS, TestTaskSpec::class.java.name)
                .build(),
            runAttemptCount = 0 // without setting this explicitly attempts in tests are counted starting from 1 instead of 0 like in production code
        ).build()

        TestTaskSpec.reset()
    }

    @Test
    fun `when task returns true work should succeed`() {
        TestTaskSpec.doReturn(true)
        assertThat(worker.doWork(), `is`(ListenableWorker.Result.success()))
    }

    @Test
    fun `when task returns false, retries if maxRetries not specified`() {
        TestTaskSpec.doReturn(false)
        assertThat(worker.doWork(), `is`(ListenableWorker.Result.retry()))
    }

    @Test
    fun `when task returns false, retries if maxRetries is specified and is higher than runAttemptCount`() {
        TestTaskSpec
            .withMaxRetries(1)
            .doReturn(false)

        assertThat(worker.doWork(), `is`(ListenableWorker.Result.retry()))
    }

    @Test
    fun `when task returns false, fails if maxRetries is specified and is equal to runAttemptCount`() {
        TestTaskSpec
            .withMaxRetries(0)
            .doReturn(false)

        assertThat(worker.doWork(), `is`(ListenableWorker.Result.failure()))
    }

    @Test
    fun `when task returns false, fails if maxRetries is specified and is lower than runAttemptCount`() {
        TestTaskSpec
            .withMaxRetries(-1)
            .doReturn(false)

        assertThat(worker.doWork(), `is`(ListenableWorker.Result.failure()))
    }

    @Test
    fun `when maxRetries is not specified, task called with isLastUniqueExecution true`() {
        TestTaskSpec
            .doReturn(false)

        worker.doWork()
        assertThat(TestTaskSpec.wasLastUniqueExecution, equalTo(true))
    }

    @Test
    fun `when maxRetries is specified and it is higher than runAttemptCount, task called with isLastUniqueExecution false`() {
        TestTaskSpec
            .withMaxRetries(1)
            .doReturn(false)

        worker.doWork()
        assertThat(TestTaskSpec.wasLastUniqueExecution, equalTo(false))
    }

    @Test
    fun `when maxRetries is specified and it is equal to runAttemptCount, task called with isLastUniqueExecution true`() {
        TestTaskSpec
            .withMaxRetries(0)
            .doReturn(false)

        worker.doWork()
        assertThat(TestTaskSpec.wasLastUniqueExecution, equalTo(true))
    }

    @Test
    fun `when maxRetries is specified and it is lower than runAttemptCount, task called with isLastUniqueExecution true`() {
        TestTaskSpec
            .withMaxRetries(-1)
            .doReturn(false)

        worker.doWork()
        assertThat(TestTaskSpec.wasLastUniqueExecution, equalTo(true))
    }

    @Test
    fun `when there is an exception, calls onException`() {
        val exception = IllegalStateException()

        TestTaskSpec
            .doThrow(exception)

        worker.doWork()
        assertThat(TestTaskSpec.onExceptionCalledWith, equalTo(exception))
    }
}

private class TestTaskSpec : TaskSpec {

    companion object {

        private var maxRetries: Int? = null
        private var returnValue = true
        private var exception: Throwable? = null

        var wasLastUniqueExecution = false
            private set

        var onExceptionCalledWith: Throwable? = null
            private set

        fun reset() {
            returnValue = true
            maxRetries = null
            exception = null
            wasLastUniqueExecution = false
            onExceptionCalledWith = null
        }

        fun doReturn(value: Boolean): Companion {
            returnValue = value
            return this
        }

        fun withMaxRetries(maxRetries: Int): Companion {
            this.maxRetries = maxRetries
            return this
        }

        fun doThrow(exception: Throwable) {
            this.exception = exception
        }
    }

    override val maxRetries: Int? = Companion.maxRetries
    override val backoffPolicy: BackoffPolicy? = null
    override val backoffDelay: Long? = null

    override fun getTask(
        context: Context,
        inputData: Map<String, String>,
        isLastUniqueExecution: Boolean,
        isStopped: (() -> Boolean)
    ): Supplier<Boolean> {
        wasLastUniqueExecution = isLastUniqueExecution

        return Supplier {
            exception?.let {
                throw it
            }

            returnValue
        }
    }

    override fun onException(exception: Throwable) {
        onExceptionCalledWith = exception
    }
}
