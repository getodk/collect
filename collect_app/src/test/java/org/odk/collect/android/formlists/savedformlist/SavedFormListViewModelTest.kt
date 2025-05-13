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
import org.mockito.kotlin.any
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
        on { getInstances(any()) } doReturn MutableStateFlow(emptyList())
    }

    @Test
    fun `formsToDisplay should not include deleted forms`() {
        val myForm = InstanceFixtures.instance(displayName = "My form", deletedDate = 1)
        val yourForm = InstanceFixtures.instance(displayName = "Your form")
        saveForms("projectId", listOf(myForm, yourForm))

        val viewModel = SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")

        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(yourForm)
        )
    }

    @Test
    fun `setting filterText filters forms on display name`() {
        val myForm = InstanceFixtures.instance(displayName = "My form")
        val yourForm = InstanceFixtures.instance(displayName = "Your form")
        saveForms("projectId", listOf(myForm, yourForm),)

        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")

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
        saveForms("projectId", listOf(myForm, yourForm),)

        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")

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
        saveForms("projectId", listOf(myForm, yourForm),)

        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")

        viewModel.filterText = "my"
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(myForm)
        )
    }

    @Test
    fun `can sort forms by ascending name`() {
        val aa = InstanceFixtures.instance(displayName = "Aa")
        val ab = InstanceFixtures.instance(displayName = "ab")
        val b = InstanceFixtures.instance(displayName = "B")
        saveForms("projectId", listOf(b, aa, ab),)

        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")

        viewModel.sortOrder = SortOrder.NAME_ASC
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(aa, ab, b)
        )
    }

    @Test
    fun `can sort forms by descending name`() {
        val aa = InstanceFixtures.instance(displayName = "Aa")
        val ab = InstanceFixtures.instance(displayName = "ab")
        val b = InstanceFixtures.instance(displayName = "B")
        saveForms("projectId", listOf(b, aa, ab),)

        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")

        viewModel.sortOrder = SortOrder.NAME_DESC
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(b, ab, aa)
        )
    }

    @Test
    fun `can sort forms by descending date`() {
        val a = InstanceFixtures.instance(displayName = "A", lastStatusChangeDate = 0)
        val b = InstanceFixtures.instance(displayName = "B", lastStatusChangeDate = 1)
        saveForms("projectId", listOf(a, b),)

        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")

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
        saveForms("projectId", listOf(b, a),)

        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")

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
        saveForms("projectId", listOf(b, a),)

        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")
        viewModel.sortOrder = SortOrder.DATE_ASC

        val newViewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")
        assertThat(newViewModel.sortOrder, equalTo(SortOrder.DATE_ASC))
        assertThat(
            newViewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(a, b)
        )
    }

    @Test
    fun `isDeleting is true while deleting forms`() {
        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")
        assertThat(viewModel.isDeleting.getOrAwaitValue(), equalTo(false))

        viewModel.deleteForms(longArrayOf(1))
        assertThat(viewModel.isDeleting.getOrAwaitValue(), equalTo(true))

        scheduler.flush()
        assertThat(viewModel.isDeleting.getOrAwaitValue(), equalTo(false))
    }

    @Test
    fun `deleteForms should return 0 if instances can not be deleted`() {
        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")
        whenever(instancesDataService.deleteInstances(any(), any())).thenReturn(false)

        val result = viewModel.deleteForms(longArrayOf(1))
        assertThat(result.getOrAwaitValue(scheduler)!!.value, equalTo(0))
    }

    @Test
    fun `deleteForms should return the number of instances after deleting`() {
        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")
        whenever(instancesDataService.deleteInstances(any(), any())).thenReturn(true)

        val result = viewModel.deleteForms(longArrayOf(1))
        assertThat(result.getOrAwaitValue(scheduler)!!.value, equalTo(1))
    }

    @Test
    fun `filtering takes into account edit numbers`() {
        val originalInstance = InstanceFixtures.instance(displayName = "My form")
        val editedInstance = InstanceFixtures.instance(displayName = "My form", editOf = 1, editNumber = 1)
        saveForms("projectId", listOf(originalInstance, editedInstance))

        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")

        viewModel.filterText = "Edit 1"
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(editedInstance)
        )
    }

    @Test
    fun `sorting takes into account edit numbers`() {
        val instance1 = InstanceFixtures.instance(displayName = "My form", editOf = 1, editNumber = 1)
        val instance2 = InstanceFixtures.instance(displayName = "My form", editOf = 1, editNumber = 2)
        val instance3 = InstanceFixtures.instance(displayName = "My form", editOf = 1, editNumber = 3)
        saveForms("projectId", listOf(instance1, instance2, instance3),)

        val viewModel =
            SavedFormListViewModel(scheduler, settings, instancesDataService, "projectId")

        viewModel.sortOrder = SortOrder.NAME_DESC
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(instance3, instance2, instance1)
        )

        viewModel.sortOrder = SortOrder.NAME_ASC
        assertThat(
            viewModel.formsToDisplay.getOrAwaitValue(scheduler),
            contains(instance1, instance2, instance3)
        )
    }

    private fun saveForms(projectId: String, instances: List<Instance>) {
        whenever(instancesDataService.getInstances(projectId)).doReturn(MutableStateFlow(instances))
    }
}
