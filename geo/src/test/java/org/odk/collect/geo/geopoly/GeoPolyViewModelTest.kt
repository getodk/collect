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
import org.odk.collect.geo.support.FakeLocationTracker
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.shared.settings.InMemSettings
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
            FakeLocationTracker(),
            mock(),
            InMemSettings(),
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
            FakeLocationTracker(),
            mock(),
            InMemSettings(),
            invalidMessage
        )

        invalidMessage.value = DisplayString.Raw("OH NO")
        invalidMessage.value = null
        invalidMessage.value = null
        assertThat(viewModel.fixedAlerts.getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun `#onCleared stops LocationTracker`() {
        val locationTracker = FakeLocationTracker()
        val viewModel = GeoPolyViewModel(
            GeoPolyFragment.OutputMode.GEOTRACE,
            emptyList(),
            false,
            locationTracker,
            mock(),
            InMemSettings(),
            MutableLiveData(null)
        )

        assertThat(locationTracker.isStarted, equalTo(true))

        viewModel.onCleared()
        assertThat(locationTracker.isStarted, equalTo(false))
    }

    @Test
    fun `#recordingMode is PLACEMENT when nothing is saved`() {
        val viewModel = createViewModel(InMemSettings())
        assertThat(viewModel.recordingMode, equalTo(GeoPolyViewModel.RecordingMode.PLACEMENT))
    }

    @Test
    fun `#recordingMode is the last saved recording mode`() {
        val metaSettings = InMemSettings().apply {
            save(MetaKeys.LAST_USED_GEOPOLY_RECORDING_MODE, GeoPolyViewModel.RecordingMode.AUTOMATIC.name)
        }

        val viewModel = createViewModel(metaSettings)
        assertThat(viewModel.recordingMode, equalTo(GeoPolyViewModel.RecordingMode.AUTOMATIC))
    }

    @Test
    fun `#recordingMode is PLACEMENT when the saved recording mode is unknown`() {
        val metaSettings = InMemSettings().apply {
            save(MetaKeys.LAST_USED_GEOPOLY_RECORDING_MODE, "UNKNOWN")
        }

        val viewModel = createViewModel(metaSettings)
        assertThat(viewModel.recordingMode, equalTo(GeoPolyViewModel.RecordingMode.PLACEMENT))
    }

    private fun createViewModel(metaSettings: InMemSettings): GeoPolyViewModel {
        return GeoPolyViewModel(
            GeoPolyFragment.OutputMode.GEOTRACE,
            emptyList(),
            false,
            FakeLocationTracker(),
            mock(),
            metaSettings,
            MutableLiveData(null)
        )
    }
}
