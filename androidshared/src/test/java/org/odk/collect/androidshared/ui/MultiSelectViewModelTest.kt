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
}
