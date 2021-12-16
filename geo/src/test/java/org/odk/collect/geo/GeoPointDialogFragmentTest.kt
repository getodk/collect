package org.odk.collect.geo

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.fragmentstest.DialogFragmentTest
import org.odk.collect.fragmentstest.DialogFragmentTest.onViewInDialog
import org.odk.collect.location.Location
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class GeoPointDialogFragmentTest {

    private val scheduler = FakeScheduler()

    private val locationLiveData: MutableLiveData<Location?> = MutableLiveData(null)
    private val currentAccuracyLiveData: MutableLiveData<Float?> = MutableLiveData(null)
    private val viewModel = mock<GeoPointViewModel> {
        on { location } doReturn locationLiveData
        on { currency } doReturn currentAccuracyLiveData
    }

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

        currentAccuracyLiveData.value = null
        scheduler.runForeground()
        onViewInDialog(withText("--")).check(matches(isDisplayed()))

        currentAccuracyLiveData.value = 50.2f
        scheduler.runForeground()
        onViewInDialog(withText("50.2m")).check(matches(isDisplayed()))

        currentAccuracyLiveData.value = 15.65f
        scheduler.runForeground()
        onViewInDialog(withText("15.65m")).check(matches(isDisplayed()))
    }

    @Test
    fun `clicking cancel calls listener`() {
        val scenario = DialogFragmentTest.launchDialogFragment(GeoPointDialogFragment::class.java)

        val listener = mock<GeoPointDialogFragment.Listener>()
        scenario.onFragment {
            it.listener = listener
        }

        onViewInDialog(withText(R.string.cancel)).perform(click())
        verify(listener).onCancel()
    }
}
