package org.odk.collect.android.instancemanagement.send

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.shared.TimeInMs
import org.odk.collect.testshared.FakeScheduler

class ReadyToSendViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private var instancesRepository = InMemInstancesRepository().also {
        it.save(
            Instance.Builder()
                .formId("1")
                .status(Instance.STATUS_INCOMPLETE)
                .lastStatusChangeDate(TimeInMs.ONE_SECOND * 1)
                .build()
        )

        it.save(
            Instance.Builder()
                .formId("2")
                .status(Instance.STATUS_COMPLETE)
                .lastStatusChangeDate(TimeInMs.ONE_SECOND * 5)
                .build()
        )

        it.save(
            Instance.Builder()
                .formId("3")
                .status(Instance.STATUS_SUBMITTED)
                .lastStatusChangeDate(TimeInMs.ONE_SECOND * 4)
                .build()
        )

        it.save(
            Instance.Builder()
                .formId("4")
                .status(Instance.STATUS_SUBMITTED)
                .lastStatusChangeDate(TimeInMs.ONE_SECOND * 6)
                .build()
        )

        it.save(
            Instance.Builder()
                .formId("5")
                .status(Instance.STATUS_SUBMISSION_FAILED)
                .lastStatusChangeDate(TimeInMs.ONE_SECOND * 7)
                .build()
        )

        it.save(
            Instance.Builder()
                .formId("6")
                .status(Instance.STATUS_COMPLETE)
                .lastStatusChangeDate(TimeInMs.ONE_SECOND * 3)
                .build()
        )

        it.save(
            Instance.Builder()
                .formId("7")
                .status(Instance.STATUS_COMPLETE)
                .lastStatusChangeDate(TimeInMs.ONE_SECOND * 10)
                .build()
        )
    }
    private val scheduler = FakeScheduler()

    private val viewModel = ReadyToSendViewModel(instancesRepository, scheduler) { TimeInMs.ONE_SECOND * 10 }

    @Test
    fun `numberOfSentInstances should represent the real number of instances with STATUS_SUBMITTED in the database`() {
        scheduler.runBackground()
        assertThat(viewModel.data.value!!.numberOfSentInstances, equalTo(2))
    }

    @Test
    fun `numberOfInstancesReadyToSend should represent the real number of instances with STATUS_COMPLETE and STATUS_SUBMISSION_FAILED in the database`() {
        scheduler.runBackground()
        assertThat(viewModel.data.value!!.numberOfInstancesReadyToSend, equalTo(4))
    }

    @Test
    fun `lastInstanceSentTimeMillis should be correctly calculate when the last instance has been sent`() {
        scheduler.runBackground()
        assertThat(viewModel.data.value!!.lastInstanceSentTimeMillis, equalTo(4000L))
    }

    @Test
    fun `lastInstanceSentTimeMillis should be 0 if there are no sent instances`() {
        instancesRepository.deleteAll()
        scheduler.runBackground()
        assertThat(viewModel.data.value!!.lastInstanceSentTimeMillis, equalTo(0L))
    }
}
