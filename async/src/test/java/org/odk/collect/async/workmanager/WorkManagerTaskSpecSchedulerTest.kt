package org.odk.collect.async.workmanager

import android.content.Context
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.BackoffPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import java.util.UUID
import java.util.function.Supplier
import kotlin.time.Duration.Companion.minutes

@RunWith(AndroidJUnit4::class)
class WorkManagerTaskSpecSchedulerTest {

    private val applicationContext = ApplicationProvider.getApplicationContext<Context>()
    private val workManager by lazy { WorkManager.getInstance(applicationContext) }
    private val testDriver by lazy { WorkManagerTestInitHelper.getTestDriver(applicationContext)!! }

    private val taskSpecScheduler by lazy { WorkManagerTaskSpecScheduler(workManager) }

    @Before
    fun setup() {
        WorkManagerTestInitHelper.initializeTestWorkManager(applicationContext)
        TestTaskSpec.reset()
    }

    @Test
    fun `work succeeds when task returns Success`() {
        TestTaskSpec.doReturn(TaskSpec.Result.Success())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)

        assertThat(workManager.getWorkInfo(workId).state, equalTo(WorkInfo.State.SUCCEEDED))
    }

    @Test
    fun `retries when task returns Failure and maxRetries not specified`() {
        TestTaskSpec.doReturn(TaskSpec.Result.Failure())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)

        val workInfo = workManager.getWorkInfo(workId)
        assertThat(workInfo.runAttemptCount, equalTo(1))
        assertThat(workInfo.state, equalTo(WorkInfo.State.ENQUEUED))
    }

    @Test
    fun `retries when task returns Failure with maxRetries higher than runAttemptCount`() {
        TestTaskSpec
            .withMaxRetries(1)
            .doReturn(TaskSpec.Result.Failure())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val enqueuedTasks = workManager.getEnqueuedTasks()
        assertThat(enqueuedTasks.size, equalTo(1))

        val workId = enqueuedTasks[0].id
        testDriver.setAllConstraintsMet(workId)

        val workInfo = workManager.getWorkInfo(workId)
        assertThat(workInfo.runAttemptCount, equalTo(1))
        assertThat(workInfo.state, equalTo(WorkInfo.State.ENQUEUED))
    }

    @Test
    fun `fails when task returns Failure with maxRetries equal to runAttemptCount`() {
        TestTaskSpec
            .withMaxRetries(0)
            .doReturn(TaskSpec.Result.Failure())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)

        assertThat(workManager.getWorkInfo(workId).state, equalTo(WorkInfo.State.FAILED))
    }

    @Test
    fun `fails when task returns Failure with maxRetries lower than runAttemptCount`() {
        TestTaskSpec
            .withMaxRetries(-1)
            .doReturn(TaskSpec.Result.Failure())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)

        assertThat(workManager.getWorkInfo(workId).state, equalTo(WorkInfo.State.FAILED))
    }

    @Test
    fun `runs with isLastUniqueExecution true when maxRetries not specified`() {
        TestTaskSpec
            .doReturn(TaskSpec.Result.Failure())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)

        assertThat(TestTaskSpec.wasLastUniqueExecution, equalTo(true))
    }

    @Test
    fun `runs with isLastUniqueExecution false when maxRetries higher than runAttemptCount`() {
        TestTaskSpec
            .withMaxRetries(1)
            .doReturn(TaskSpec.Result.Failure())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)

        assertThat(TestTaskSpec.wasLastUniqueExecution, equalTo(false))
    }

    @Test
    fun `runs with isLastUniqueExecution true when maxRetries equal to runAttemptCount`() {
        TestTaskSpec
            .withMaxRetries(0)
            .doReturn(TaskSpec.Result.Failure())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)

        assertThat(TestTaskSpec.wasLastUniqueExecution, equalTo(true))
    }

    @Test
    fun `runs with isLastUniqueExecution true when maxRetries lower than runAttemptCount`() {
        TestTaskSpec
            .withMaxRetries(-1)
            .doReturn(TaskSpec.Result.Failure())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)

        assertThat(TestTaskSpec.wasLastUniqueExecution, equalTo(true))
    }

    @Test
    fun `fails when task throws exception`() {
        val exception = IllegalStateException()

        TestTaskSpec
            .doThrow(exception)

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)

        assertThat(workManager.getWorkInfo(workId).state, equalTo(WorkInfo.State.FAILED))
        assertThat(TestTaskSpec.onExceptionCalledWith, equalTo(exception))
    }

    @Test
    fun `sets connected constraints when networkConstraint is not specified`() {
        TestTaskSpec.doReturn(TaskSpec.Result.Success())

        taskSpecScheduler.schedule(
            tag = "cellular",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val workInfo = workManager.getEnqueuedTask()
        val requiredNetworkRequest = workInfo.constraints.requiredNetworkRequest
        assertThat(
            requiredNetworkRequest?.capabilities?.asIterable(),
            hasItem(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        )
        assertThat(requiredNetworkRequest?.transportTypes?.isEmpty(), equalTo(true))
    }

    @Test
    fun `sets cellular constraints when networkConstraint is specified`() {
        TestTaskSpec.doReturn(TaskSpec.Result.Success())

        taskSpecScheduler.schedule(
            tag = "cellular",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = Scheduler.NetworkType.CELLULAR,
            repeatPeriod = null
        )

        val workInfo = workManager.getEnqueuedTask()
        val requiredNetworkRequest = workInfo.constraints.requiredNetworkRequest
        assertThat(
            requiredNetworkRequest?.capabilities?.asIterable(),
            hasItem(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        )
        assertThat(
            requiredNetworkRequest?.transportTypes?.asIterable(),
            hasItem(NetworkCapabilities.TRANSPORT_CELLULAR)
        )
    }

    @Test
    fun `sets wifi constraints when networkConstraint is specified`() {
        TestTaskSpec.doReturn(TaskSpec.Result.Success())

        taskSpecScheduler.schedule(
            tag = "wifi",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = Scheduler.NetworkType.WIFI,
            repeatPeriod = null
        )

        val workInfo = workManager.getEnqueuedTask()
        val requiredNetworkRequest = workInfo.constraints.requiredNetworkRequest
        assertThat(
            requiredNetworkRequest?.capabilities?.asIterable(),
            hasItem(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        )
        assertThat(
            requiredNetworkRequest?.transportTypes?.asIterable(),
            hasItem(NetworkCapabilities.TRANSPORT_WIFI)
        )
    }

    @Test
    fun `sets periodicity correctly when repeatPeriod specified`() {
        TestTaskSpec.doReturn(TaskSpec.Result.Success())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = 15.minutes.inWholeMilliseconds
        )

        val workId = workManager.getEnqueuedTask().id
        assertThat(
            workManager.getWorkInfo(workId).periodicityInfo!!.repeatIntervalMillis,
            equalTo(15.minutes.inWholeMilliseconds)
        )
    }

    @Test
    fun `runs task repeatedly when repeatPeriod specified`() {
        TestTaskSpec.doReturn(TaskSpec.Result.Success())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = 15.minutes.inWholeMilliseconds
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)
        testDriver.setAllConstraintsMetForEnqueued(workManager)
        assertThat(TestTaskSpec.runCount, equalTo(1))

        testDriver.setPeriodDelayMet(workId)
        testDriver.setAllConstraintsMet(workId)
        testDriver.setAllConstraintsMetForEnqueued(workManager)
        assertThat(TestTaskSpec.runCount, equalTo(2))
    }

    @Test
    fun `replaces existing task when repeatPeriod specified and task left from last repeat`() {
        TestTaskSpec.doReturn(TaskSpec.Result.Success())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = 15.minutes.inWholeMilliseconds
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)
        assertThat(TestTaskSpec.runCount, equalTo(0))

        testDriver.setPeriodDelayMet(workId)
        testDriver.setAllConstraintsMet(workId)
        testDriver.setAllConstraintsMetForEnqueued(workManager)
        assertThat(TestTaskSpec.runCount, equalTo(1))
    }

    @Test
    fun `does not replace existing task when task left from different repeat`() {
        TestTaskSpec.doReturn(TaskSpec.Result.Success())

        taskSpecScheduler.schedule(
            tag = "tag1",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = 15.minutes.inWholeMilliseconds
        )

        testDriver.setAllConstraintsMet(workManager.getWorkInfosByTag("tag1").get()[0].id)

        taskSpecScheduler.schedule(
            tag = "tag2",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = 15.minutes.inWholeMilliseconds
        )

        testDriver.setAllConstraintsMet(workManager.getWorkInfosByTag("tag2").get()[0].id)
        testDriver.setAllConstraintsMetForEnqueued(workManager)
        assertThat(TestTaskSpec.runCount, equalTo(2))
    }

    @Test
    fun `sets constraints for each task in the chain when there is a repeat period`() {
        TestTaskSpec.doReturn(TaskSpec.Result.Success())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec(), TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = Scheduler.NetworkType.WIFI,
            repeatPeriod = 15.minutes.inWholeMilliseconds
        )

        val workId = workManager.getEnqueuedTask().id
        testDriver.setAllConstraintsMet(workId)

        val firstStep = workManager.getEnqueuedTask(periodic = false)
        assertThat(
            firstStep.constraints.requiredNetworkRequest?.capabilities?.asIterable(),
            hasItem(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        )
        assertThat(
            firstStep.constraints.requiredNetworkRequest?.transportTypes?.asIterable(),
            hasItem(NetworkCapabilities.TRANSPORT_WIFI)
        )

        testDriver.setAllConstraintsMet(firstStep.id)

        val secondStep = workManager.getEnqueuedTask(periodic = false)
        assertThat(
            secondStep.constraints.requiredNetworkRequest?.capabilities?.asIterable(),
            hasItem(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        )
        assertThat(
            secondStep.constraints.requiredNetworkRequest?.transportTypes?.asIterable(),
            hasItem(NetworkCapabilities.TRANSPORT_WIFI)
        )
    }
}

private fun WorkManager.getEnqueuedTask(periodic: Boolean = true): WorkInfo {
    val enqueuedTasks = this.getEnqueuedTasks().filter { periodic || it.periodicityInfo == null }
    assertThat(enqueuedTasks.size, equalTo(1))
    return enqueuedTasks[0]
}

private fun WorkManager.getEnqueuedTasks(): List<WorkInfo> {
    return getWorkInfos(WorkQuery.fromStates(WorkInfo.State.ENQUEUED)).get()!!
}

private fun WorkManager.getWorkInfo(workId: UUID): WorkInfo {
    return getWorkInfoById(workId).get()!!
}

private fun TestDriver.setAllConstraintsMetForEnqueued(workManager: WorkManager) {
    workManager.getEnqueuedTasks().forEach {
        try {
            this.setAllConstraintsMet(it.id)
        } catch (_: Exception) {
            // Ignored
        }
    }
}

private class TestTaskSpec : TaskSpec {

    companion object {

        private var maxRetries: Int? = null
        private var returnValue: TaskSpec.Result = TaskSpec.Result.Success()
        private var exception: Throwable? = null

        var wasLastUniqueExecution = false
            private set

        var onExceptionCalledWith: Throwable? = null
            private set

        var runCount: Int = 0
            private set

        fun reset() {
            returnValue = TaskSpec.Result.Success()
            maxRetries = null
            exception = null
            wasLastUniqueExecution = false
            onExceptionCalledWith = null
            runCount = 0
        }

        fun doReturn(value: TaskSpec.Result): Companion {
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
    ): Supplier<TaskSpec.Result> {
        wasLastUniqueExecution = isLastUniqueExecution

        return Supplier {
            runCount++
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
