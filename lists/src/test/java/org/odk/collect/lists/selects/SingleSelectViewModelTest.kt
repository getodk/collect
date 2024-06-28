package org.odk.collect.lists.selects

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.odk.collect.androidtest.getOrAwaitValue

class SingleSelectViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `nothing is selected on viewmodel initialization if selected item id is null`() {
        val selected = null
        val data: LiveData<List<SelectItem<*>>> = MutableLiveData(listOf(SelectItem("1", 1), SelectItem("2", 2)))
        val viewModel = SingleSelectViewModel(selected, data)

        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun `nothing is selected on viewmodel initialization if selected item id is not null but there is no item witch matching id`() {
        val selected = "0"
        val data: LiveData<List<SelectItem<*>>> = MutableLiveData(listOf(SelectItem("1", 1), SelectItem("2", 2)))
        val viewModel = SingleSelectViewModel(selected, data)

        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun `proper item is selected on viewmodel initialization if selected item id is not null`() {
        val selected = "1"
        val data: LiveData<List<SelectItem<*>>> = MutableLiveData(listOf(SelectItem("1", 1), SelectItem("2", 2)))
        val viewModel = SingleSelectViewModel(selected, data)

        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo("1"))
    }

    @Test
    fun `getSelected returns selected item id`() {
        val selected = null
        val data: LiveData<List<SelectItem<*>>> = MutableLiveData(listOf(SelectItem("1", 1), SelectItem("2", 2)))
        val viewModel = SingleSelectViewModel(selected, data)

        viewModel.select("1")
        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo("1"))

        viewModel.select("2")
        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo("2"))
    }

    @Test
    fun `clear unselects selected item`() {
        val selected = null
        val data: LiveData<List<SelectItem<*>>> = MutableLiveData(listOf(SelectItem("1", 1), SelectItem("2", 2)))
        val viewModel = SingleSelectViewModel(selected, data)

        viewModel.select("1")
        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo("1"))
        viewModel.clear()
        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo(null))
    }
}
