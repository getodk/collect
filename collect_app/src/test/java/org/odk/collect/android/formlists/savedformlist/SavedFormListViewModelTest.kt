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
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class SavedFormListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val scheduler = FakeScheduler()

    private val instances = MutableStateFlow<List<Instance>>(emptyList())
    private val instancesDataService: InstancesDataService = mock {
        on { instances } doReturn instances
    }

    @Test
    fun `setting filterText filters forms on display name`() {
        val myForm = InstanceFixtures.instance(displayName = "My form")
        val yourForm = InstanceFixtures.instance(displayName = "Your form")
        instances.value = listOf(myForm, yourForm)

        val viewModel = SavedFormListViewModel(scheduler, instancesDataService)

        viewModel.filterText = "Your"
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(),
            contains(yourForm)
        )

        viewModel.filterText = "form"
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(),
            contains(myForm, yourForm)
        )
    }

    @Test
    fun `clearing filterText does not filter forms`() {
        val myForm = InstanceFixtures.instance(displayName = "My form")
        val yourForm = InstanceFixtures.instance(displayName = "Your form")
        instances.value = listOf(myForm, yourForm)

        val viewModel = SavedFormListViewModel(scheduler, instancesDataService)

        viewModel.filterText = "blah"
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(),
            equalTo(emptyList())
        )

        viewModel.filterText = ""
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(),
            contains(myForm, yourForm)
        )
    }

    @Test
    fun `filtering forms is not case sensitive`() {
        val myForm = InstanceFixtures.instance(displayName = "My form")
        val yourForm = InstanceFixtures.instance(displayName = "Your form")
        instances.value = listOf(myForm, yourForm)

        val viewModel = SavedFormListViewModel(scheduler, instancesDataService)

        viewModel.filterText = "my"
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(),
            contains(myForm)
        )
    }
}
