package org.odk.collect.android.formlists.savedformlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.getOrAwaitValue

@RunWith(AndroidJUnit4::class)
class SavedFormListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val scheduler = FakeScheduler()

    private val instancesDataService: InstancesDataService = mock {
        on { instances } doReturn MutableStateFlow(emptyList())
    }

    @Test
    fun `setting filterText filters forms on display name`() {
        val myForm = InstanceFixtures.instance(displayName = "My form")
        val yourForm = InstanceFixtures.instance(displayName = "Your form")
        saveForms(
            listOf(
                myForm,
                yourForm
            )
        )

        val viewModel = SavedFormListViewModel(scheduler, instancesDataService)

        viewModel.filterText = "Your"
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(yourForm)
        )

        viewModel.filterText = "form"
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(myForm, yourForm)
        )
    }

    @Test
    fun `clearing filterText does not filter forms`() {
        val myForm = InstanceFixtures.instance(displayName = "My form")
        val yourForm = InstanceFixtures.instance(displayName = "Your form")
        saveForms(
            listOf(
                myForm,
                yourForm
            )
        )

        val viewModel = SavedFormListViewModel(scheduler, instancesDataService)

        viewModel.filterText = "blah"
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            equalTo(emptyList())
        )

        viewModel.filterText = ""
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(myForm, yourForm)
        )
    }

    @Test
    fun `filtering forms is not case sensitive`() {
        val myForm = InstanceFixtures.instance(displayName = "My form")
        val yourForm = InstanceFixtures.instance(displayName = "Your form")
        saveForms(
            listOf(
                myForm,
                yourForm
            )
        )

        val viewModel = SavedFormListViewModel(scheduler, instancesDataService)

        viewModel.filterText = "my"
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(myForm)
        )
    }

    private fun saveForms(instances: List<Instance>) {
        whenever(instancesDataService.instances).doReturn(MutableStateFlow(instances))
    }
}
