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
import org.mockito.kotlin.whenever
import org.odk.collect.fragmentstest.DialogFragmentTest
import org.odk.collect.fragmentstest.DialogFragmentTest.onViewInDialog
import org.odk.collect.location.Location
import org.odk.collect.strings.localization.getLocalizedString
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class GeoPointDialogFragmentTest {

    private val application = getApplicationContext<RobolectricApplication>()
    private val scheduler = FakeScheduler()

    private val locationLiveData: MutableLiveData<Location?> = MutableLiveData(null)
    private val currentAccuracyLiveData: MutableLiveData<Float?> = MutableLiveData(null)
    private val timeElapsedLiveData: MutableLiveData<Long> = MutableLiveData(0)
    private val viewModel = mock<GeoPointViewModel> {
        on { location } doReturn locationLiveData
        on { currentAccuracy } doReturn currentAccuracyLiveData
        on { timeElapsed } doReturn timeElapsedLiveData
    }

    @Before
    fun setup() {
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
    fun `shows accuracy threshold`() {
        whenever(viewModel.accuracyThreshold).thenReturn(5.0)
        DialogFragmentTest.launchDialogFragment(GeoPointDialogFragment::class.java)

        onViewInDialog(
            withText(
                application.getLocalizedString(
                    R.string.point_will_be_saved,
                    "5m"
                )
            )
        ).check(
            matches(isDisplayed())
        )
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
    fun `shows and updates qualitative feedback`() {
        whenever(viewModel.accuracyThreshold).thenReturn(5.0)
        DialogFragmentTest.launchDialogFragment(GeoPointDialogFragment::class.java)

        currentAccuracyLiveData.value = 9.2f
        scheduler.runForeground()
        onViewInDialog(
            withText(
                application.getLocalizedString(
                    R.string.distance_from_accuracy_goal,
                    "4.2m",
                    "5m"
                )
            )
        ).check(matches(isDisplayed()))

        currentAccuracyLiveData.value = 7.52f
        scheduler.runForeground()
        onViewInDialog(
            withText(
                application.getLocalizedString(
                    R.string.distance_from_accuracy_goal,
                    "2.52m",
                    "5m"
                )
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun `shows and updates time elapsed`() {
        DialogFragmentTest.launchDialogFragment(GeoPointDialogFragment::class.java)

        timeElapsedLiveData.value = 0
        scheduler.runForeground()
        onViewInDialog(
            withText(
                application.getLocalizedString(
                    R.string.time_elapsed,
                    "00:00"
                )
            )
        ).check(
            matches(isDisplayed())
        )

        timeElapsedLiveData.value = 62000
        scheduler.runForeground()
        onViewInDialog(
            withText(
                application.getLocalizedString(
                    R.string.time_elapsed,
                    "01:02"
                )
            )
        ).check(
            matches(isDisplayed())
        )
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

    @Test
    fun `clicking save calls forceLocation() on view model`() {
        DialogFragmentTest.launchDialogFragment(GeoPointDialogFragment::class.java)
        onViewInDialog(withText(R.string.save)).perform(click())
        verify(viewModel).forceLocation()
    }
}
