package org.odk.collect.geo

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.androidtest.Extensions.isFinishing
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY
import org.odk.collect.location.Location
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class GeoPointActivityTest {

    private val locationLiveData: MutableLiveData<Location?> = MutableLiveData(null)
    private val viewModel = mock<GeoPointViewModel> {
        on { acceptedLocation } doReturn locationLiveData
        on { currentAccuracy } doReturn MutableLiveData(null)
        on { timeElapsed } doReturn MutableNonNullLiveData(0)
        on { satellites } doReturn MutableNonNullLiveData(0)
    }

    private val scheduler = FakeScheduler()

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Before
    fun setup() {
        val application = getApplicationContext<RobolectricApplication>()
        application.geoDependencyComponent = DaggerGeoDependencyComponent.builder()
            .application(application)
            .geoDependencyModule(object : GeoDependencyModule() {
                override fun providesScheduler() = scheduler

                override fun providesGeoPointViewModelFactory(application: Application) =
                    mock<GeoPointViewModelFactory> {
                        on { create(GeoPointViewModel::class.java) } doReturn viewModel
                    }
            })
            .build()
    }

    @Test
    fun `starts view model`() {
        val intent = Intent(getApplicationContext(), GeoPointActivity::class.java)

        launcherRule.launch<GeoPointActivity>(intent)
        verify(viewModel).start(retainMockAccuracy = false,)
    }

    @Test
    fun `shows dialog`() {
        val scenario = launcherRule.launch(GeoPointActivity::class.java)
        scenario.onActivity {
            val fragments = it.supportFragmentManager.fragments
            assertThat(fragments[0].javaClass, equalTo(GeoPointDialogFragment::class.java))
        }
    }

    @Test
    fun `finishes with location when available`() {
        val scenario = launcherRule.launch(GeoPointActivity::class.java)

        val location = Location(0.0, 0.0, 0.0, 0.0f)
        locationLiveData.value = location

        assertThat(scenario.isFinishing, equalTo(true))
        assertThat(scenario.result.resultCode, equalTo(Activity.RESULT_OK))
        val resultIntent = scenario.result.resultData
        val returnedValue = ExternalAppUtils.getReturnedSingleValue(resultIntent)
        assertThat(
            returnedValue,
            equalTo(GeoUtils.formatLocationResultString(location))
        )
    }

    @Test
    fun `finishes when dialog is cancelled`() {
        val scenario = launcherRule.launch(GeoPointActivity::class.java)
        scenario.onActivity {
            it.onCancel()
        }

        assertThat(scenario.isFinishing, equalTo(true))
        assertThat(scenario.result.resultCode, equalTo(Activity.RESULT_CANCELED))
    }

    @Test
    fun `passes retain mock accuracy extra to view model`() {
        val intent = Intent(getApplicationContext(), GeoPointActivity::class.java)

        intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, true)
        launcherRule.launch<GeoPointActivity>(intent)
        verify(viewModel).start(retainMockAccuracy = true,)
    }

    @Test
    fun `passes threshold extra to view model`() {
        val intent = Intent(getApplicationContext(), GeoPointActivity::class.java)
        intent.putExtra(GeoPointActivity.EXTRA_ACCURACY_THRESHOLD, 5.0f)

        launcherRule.launch<GeoPointActivity>(intent)
        verify(viewModel).start(retainMockAccuracy = false, accuracyThreshold = 5.0f,)
    }

    @Test
    fun `passes unacceptable threshold extra to view model`() {
        val intent = Intent(getApplicationContext(), GeoPointActivity::class.java)
        intent.putExtra(GeoPointActivity.EXTRA_UNACCEPTABLE_ACCURACY_THRESHOLD, 10.0f)

        launcherRule.launch<GeoPointActivity>(intent)
        verify(viewModel).start(retainMockAccuracy = false, unacceptableAccuracyThreshold = 10.0f)
    }
}
