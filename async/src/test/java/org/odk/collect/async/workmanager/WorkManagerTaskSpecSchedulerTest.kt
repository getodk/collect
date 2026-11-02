package org.odk.collect.async.workmanager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.testing.WorkManagerTestInitHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.async.TaskSpec

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
    fun `when task returns true work should succeed`() {
        TestTaskSpec.doReturn(TaskSpec.Result.Success())

        taskSpecScheduler.schedule(
            tag = "tag",
            specs = listOf(TestTaskSpec()),
            inputData = emptyMap(),
            networkConstraint = null,
            repeatPeriod = null
        )

        val enqueuedTasks =
            workManager.getWorkInfos(WorkQuery.fromStates(WorkInfo.State.ENQUEUED)).get()
        assertThat(enqueuedTasks.size, equalTo(1))

        val workId = enqueuedTasks[0].id
        testDriver.setAllConstraintsMet(workId)

        assertThat(
            workManager.getWorkInfoById(workId).get()!!.state,
            equalTo(WorkInfo.State.SUCCEEDED)
        )
    }
}

