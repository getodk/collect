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
import org.odk.collect.android.formlists.savedformlist.SavedFormListViewModel.SortOrder
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.shared.settings.InMemSettings
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.getOrAwaitValue

@RunWith(AndroidJUnit4::class)
class SavedFormListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val scheduler = FakeScheduler()
    private val settings = InMemSettings()

    private val instancesDataService: InstancesDataService = mock {
        on { instances } doReturn MutableStateFlow(emptyList())
    }

    @Test
    fun `setting filterText filters forms on display name`() {
        val myForm = InstanceFixtures.instance(displayName = "My form")
        val yourForm = InstanceFixtures.instance(displayName = "Your form")
        saveForms(listOf(myForm, yourForm))

        val viewModel = SavedFormListViewModel(scheduler, settings, instancesDataService)

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
        saveForms(listOf(myForm, yourForm))

        val viewModel = SavedFormListViewModel(scheduler, settings, instancesDataService)

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
        saveForms(listOf(myForm, yourForm))

        val viewModel = SavedFormListViewModel(scheduler, settings, instancesDataService)

        viewModel.filterText = "my"
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(myForm)
        )
    }

    @Test
    fun `can sort forms by ascending name`() {
        val a = InstanceFixtures.instance(displayName = "A")
        val b = InstanceFixtures.instance(displayName = "B")
        saveForms(listOf(b, a))

        val viewModel = SavedFormListViewModel(scheduler, settings, instancesDataService)

        viewModel.sortOrder = SortOrder.NAME_ASC
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(a, b)
        )
    }

    @Test
    fun `can sort forms by descending name`() {
        val a = InstanceFixtures.instance(displayName = "A")
        val b = InstanceFixtures.instance(displayName = "B")
        saveForms(listOf(a, b))

        val viewModel = SavedFormListViewModel(scheduler, settings, instancesDataService)

        viewModel.sortOrder = SortOrder.NAME_DESC
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(b, a)
        )
    }

    @Test
    fun `can sort forms by descending date`() {
        val a = InstanceFixtures.instance(displayName = "A", lastStatusChangeDate = 0)
        val b = InstanceFixtures.instance(displayName = "B", lastStatusChangeDate = 1)
        saveForms(listOf(a, b))

        val viewModel = SavedFormListViewModel(scheduler, settings, instancesDataService)

        viewModel.sortOrder = SortOrder.DATE_DESC
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(b, a)
        )
    }

    @Test
    fun `can sort forms by ascending date`() {
        val a = InstanceFixtures.instance(displayName = "A", lastStatusChangeDate = 0)
        val b = InstanceFixtures.instance(displayName = "B", lastStatusChangeDate = 1)
        saveForms(listOf(b, a))

        val viewModel = SavedFormListViewModel(scheduler, settings, instancesDataService)

        viewModel.sortOrder = SortOrder.DATE_ASC
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(a, b)
        )
    }

    @Test
    fun `sort order is retained between view models`() {
        val a = InstanceFixtures.instance(displayName = "A", lastStatusChangeDate = 0)
        val b = InstanceFixtures.instance(displayName = "B", lastStatusChangeDate = 1)
        saveForms(listOf(b, a))

        val viewModel = SavedFormListViewModel(scheduler, settings, instancesDataService)
        viewModel.sortOrder = SortOrder.DATE_ASC

        val newViewModel = SavedFormListViewModel(scheduler, settings, instancesDataService)
        assertThat(newViewModel.sortOrder, equalTo(SortOrder.DATE_ASC))
        assertThat(
            newViewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(a, b)
        )
    }

    @Test
    fun `isDeleting is true while deleting forms`() {
        val viewModel = SavedFormListViewModel(scheduler, settings, instancesDataService)
        assertThat(viewModel.isDeleting.getOrAwaitValue(), equalTo(false))

        viewModel.deleteForms(longArrayOf(1))
        assertThat(viewModel.isDeleting.getOrAwaitValue(), equalTo(true))

        scheduler.flush()
        assertThat(viewModel.isDeleting.getOrAwaitValue(), equalTo(false))
    }

    private fun saveForms(instances: List<Instance>) {
        whenever(instancesDataService.instances).doReturn(MutableStateFlow(instances))
    }
}
