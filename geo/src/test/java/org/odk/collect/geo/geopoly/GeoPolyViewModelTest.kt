package org.odk.collect.geo.geopoly

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.odk.collect.androidshared.ui.DisplayString
import org.odk.collect.androidtest.MainDispatcherRule
import org.odk.collect.testshared.getOrAwaitValue

class GeoPolyViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `#fixedAlerts is null until after invalid message is non-null`() {
        val invalidMessage = MutableLiveData<DisplayString?>(null)
        val viewModel = GeoPolyViewModel(
            GeoPolyFragment.OutputMode.GEOTRACE,
            emptyList(),
            false,
            mock(),
            mock(),
            invalidMessage
        )

        assertThat(viewModel.fixedAlerts.getOrAwaitValue(), equalTo(null))

        invalidMessage.value = DisplayString.Raw("OH NO")
        assertThat(viewModel.fixedAlerts.getOrAwaitValue(), equalTo(null))

        invalidMessage.value = null
        assertThat(viewModel.fixedAlerts.getOrAwaitValue()!!.value, equalTo(Unit))
    }

    @Test
    fun `#fixedAlerts is null after repeated invalid message nulls after a non-null`() {
        val invalidMessage = MutableLiveData<DisplayString?>(null)
        val viewModel = GeoPolyViewModel(
            GeoPolyFragment.OutputMode.GEOTRACE,
            emptyList(),
            false,
            mock(),
            mock(),
            invalidMessage
        )

        invalidMessage.value = DisplayString.Raw("OH NO")
        invalidMessage.value = null
        invalidMessage.value = null
        assertThat(viewModel.fixedAlerts.getOrAwaitValue(), equalTo(null))
    }
}
