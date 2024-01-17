package org.odk.collect.androidshared.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test

class MultiSelectViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `getSelected returns selected`() {
        val viewModel = MultiSelectViewModel()
        viewModel.select(1)
        viewModel.select(11)

        assertThat(viewModel.getSelected().value, equalTo(setOf<Long>(1, 11)))
    }

    @Test
    fun `getSelected does not return unselected items`() {
        val viewModel = MultiSelectViewModel()
        viewModel.select(1)
        viewModel.select(11)
        viewModel.unselect(1)

        assertThat(viewModel.getSelected().value, equalTo(setOf<Long>(11)))
    }

    @Test
    fun `unselectAll unselects all items`() {
        val viewModel = MultiSelectViewModel()
        viewModel.select(1)
        viewModel.select(11)
        viewModel.unselectAll()

        assertThat(viewModel.getSelected().value, equalTo(emptySet()))
    }

    @Test
    fun `toggle changes item back and forth`() {
        val viewModel = MultiSelectViewModel()

        viewModel.toggle(1)
        viewModel.toggle(11)
        assertThat(viewModel.getSelected().value, equalTo(setOf<Long>(1, 11)))

        viewModel.toggle(11)
        assertThat(viewModel.getSelected().value, equalTo(setOf<Long>(1)))
    }

    @Test
    fun `selectAll selects all data`() {
        val viewModel = MultiSelectViewModel()
        viewModel.data = setOf(1, 2, 3)

        viewModel.selectAll()
        assertThat(viewModel.getSelected().value, equalTo(setOf<Long>(1, 2, 3)))
    }

    @Test
    fun `isAllSelected is true when all data selected`() {
        val viewModel = MultiSelectViewModel()
        viewModel.data = setOf(1, 2, 3)
        assertThat(viewModel.isAllSelected().value, equalTo(false))

        viewModel.select(1)
        viewModel.select(2)
        assertThat(viewModel.isAllSelected().value, equalTo(false))

        viewModel.select(3)
        assertThat(viewModel.isAllSelected().value, equalTo(true))
    }

    @Test
    fun `isAllSelected returns false when no data`() {
        val viewModel = MultiSelectViewModel()
        viewModel.data = setOf()
        assertThat(viewModel.isAllSelected().value, equalTo(false))
    }
}
