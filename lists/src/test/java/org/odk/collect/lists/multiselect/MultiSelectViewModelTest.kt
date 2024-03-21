package org.odk.collect.lists.multiselect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.odk.collect.androidtest.getOrAwaitValue

class MultiSelectViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `getSelected returns selected`() {
        val viewModel = MultiSelectViewModel<Any>()
        viewModel.select(1)
        viewModel.select(11)

        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo(setOf<Long>(1, 11)))
    }

    @Test
    fun `getSelected does not return unselected items`() {
        val viewModel = MultiSelectViewModel<Any>()
        viewModel.select(1)
        viewModel.select(11)
        viewModel.unselect(1)

        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo(setOf<Long>(11)))
    }

    @Test
    fun `unselectAll unselects all items`() {
        val viewModel = MultiSelectViewModel<Any>()
        viewModel.select(1)
        viewModel.select(11)
        viewModel.unselectAll()

        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo(emptySet()))
    }

    @Test
    fun `toggle changes item back and forth`() {
        val viewModel = MultiSelectViewModel<Any>()

        viewModel.toggle(1)
        viewModel.toggle(11)
        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo(setOf<Long>(1, 11)))

        viewModel.toggle(11)
        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo(setOf<Long>(1)))
    }

    @Test
    fun `selectAll selects all data`() {
        val data = MutableLiveData(listOf(MultiSelectItem<Long>(1, 1), MultiSelectItem<Long>(2, 2)))
        val viewModel = MultiSelectViewModel(data)

        viewModel.selectAll()
        assertThat(viewModel.getSelected().getOrAwaitValue(), equalTo(setOf<Long>(1, 2)))
    }

    @Test
    fun `isAllSelected is true when all data selected`() {
        val data = MutableLiveData(listOf(MultiSelectItem<Long>(1, 1), MultiSelectItem<Long>(2, 2)))
        val viewModel = MultiSelectViewModel(data)
        assertThat(viewModel.isAllSelected().getOrAwaitValue(), equalTo(false))

        viewModel.select(1)
        assertThat(viewModel.isAllSelected().getOrAwaitValue(), equalTo(false))

        viewModel.select(2)
        assertThat(viewModel.isAllSelected().getOrAwaitValue(), equalTo(true))
    }

    @Test
    fun `isAllSelected returns false when no data`() {
        val data = MutableLiveData(listOf<MultiSelectItem<Any>>())
        val viewModel = MultiSelectViewModel(data)
        assertThat(viewModel.isAllSelected().getOrAwaitValue(), equalTo(false))
    }
}
