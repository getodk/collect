package org.odk.collect.geo

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.fragmentstest.DialogFragmentTest
import org.odk.collect.fragmentstest.DialogFragmentTest.onViewInDialog
import org.odk.collect.location.Location
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class GeoPointDialogFragmentTest {

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
    fun `shows and updates current accuracy`() {
        DialogFragmentTest.launchDialogFragment(GeoPointDialogFragment::class.java)

        whenever(viewModel.currentAccuracy).doReturn(null)
        scheduler.runForeground()
        onViewInDialog(withText("--")).check(matches(isDisplayed()))

        whenever(viewModel.currentAccuracy).doReturn(50.2f)
        scheduler.runForeground()
        onViewInDialog(withText("50.2m")).check(matches(isDisplayed()))

        whenever(viewModel.currentAccuracy).doReturn(15.65f)
        scheduler.runForeground()
        onViewInDialog(withText("15.65m")).check(matches(isDisplayed()))
    }

    @Test
    fun `calls listener when location is available`() {
        val scenario = DialogFragmentTest.launchDialogFragment(GeoPointDialogFragment::class.java)

        val listener = mock<GeoPointDialogFragment.Listener>()
        scenario.onFragment {
            it.listener = listener
        }

        scheduler.runForeground()
        verify(listener, never()).onLocationAvailable(any())

        val location = Location(0.0, 0.0, 0.0, 0f)
        whenever(viewModel.location).doReturn(location)
        scheduler.runForeground()
        verify(listener).onLocationAvailable(location)
    }

    @Test
    fun `cancels repeat when paused and restarts when resumed`() {
        val scenario = DialogFragmentTest.launchDialogFragment(GeoPointDialogFragment::class.java)
        scenario.moveToState(Lifecycle.State.STARTED)
        assertThat(scheduler.isRepeatRunning(), equalTo(false))

        scenario.moveToState(Lifecycle.State.RESUMED)
        assertThat(scheduler.isRepeatRunning(), equalTo(true))
    }
}
