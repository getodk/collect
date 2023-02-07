package org.odk.collect.androidshared.ui

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class MultiSelectViewModelTest {

    @Test
    fun `getSelected returns selected`() {
        val viewModel = MultiSelectViewModel()
        viewModel.select(1)
        viewModel.select(11)

        assertThat(viewModel.getSelected(), equalTo(setOf<Long>(1, 11)))
    }

    @Test
    fun `getSelected does not return unselected items`() {
        val viewModel = MultiSelectViewModel()
        viewModel.select(1)
        viewModel.select(11)
        viewModel.unselect(1)

        assertThat(viewModel.getSelected(), equalTo(setOf<Long>(11)))
    }
}
