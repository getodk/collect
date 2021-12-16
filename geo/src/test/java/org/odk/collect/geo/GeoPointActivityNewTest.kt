package org.odk.collect.geo

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.location.Location
import org.odk.collect.testshared.Extensions.isFinishing
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class GeoPointActivityNewTest {

    private val scheduler = FakeScheduler()
    private val viewModel = mock<GeoPointViewModel>()

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
    fun `sets threshold`() {
        val intent = Intent(getApplicationContext(), GeoPointActivityNew::class.java)
        intent.putExtra(GeoPointActivityNew.EXTRA_ACCURACY_THRESHOLD, 5.0)

        ActivityScenario.launch<GeoPointActivityNew>(intent)
        verify(viewModel).accuracyThreshold = 5.0
    }

    @Test
    fun `finishes when location is available`() {
        val scenario = ActivityScenario.launch(GeoPointActivityNew::class.java)
        scheduler.runForeground(1000L)
        assertThat(scenario.isFinishing, equalTo(false))

        val location = Location(0.0, 0.0, 0.0, 0f)
        whenever(viewModel.location).doReturn(location)
        scheduler.runForeground(2000L)

        assertThat(scenario.isFinishing, equalTo(true))
        assertThat(scenario.result.resultCode, equalTo(Activity.RESULT_OK))
        val resultIntent = scenario.result.resultData
        val returnedValue = ExternalAppUtils.getReturnedSingleValue(resultIntent)
        assertThat(returnedValue, equalTo(GeoUtils.formatLocationResultString(location)))
    }

    @Test
    fun `cancels repeat when paused and restarts when resumed`() {
        val scenario = ActivityScenario.launch(GeoPointActivityNew::class.java)
        scenario.moveToState(Lifecycle.State.STARTED)
        assertThat(scheduler.isRepeatRunning(), equalTo(false))

        scenario.moveToState(Lifecycle.State.RESUMED)
        assertThat(scheduler.isRepeatRunning(), equalTo(true))
    }
}
