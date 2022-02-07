package org.odk.collect.geo

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidtest.NestedScrollToAction.nestedScrollTo
import org.odk.collect.fragmentstest.DialogFragmentTest.onViewInDialog
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.strings.localization.getLocalizedString

@RunWith(AndroidJUnit4::class)
class GeoPointDialogFragmentTest {

    private val application = getApplicationContext<RobolectricApplication>()

    private val currentAccuracyLiveData: MutableLiveData<GeoPointAccuracy?> = MutableLiveData(null)
    private val timeElapsedLiveData: MutableNonNullLiveData<Long> = MutableNonNullLiveData(0)
    private val satellitesLiveData = MutableNonNullLiveData(0)
    private val viewModel = mock<GeoPointViewModel> {
        on { currentAccuracy } doReturn currentAccuracyLiveData
        on { timeElapsed } doReturn timeElapsedLiveData
        on { satellites } doReturn satellitesLiveData
    }

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Before
    fun setup() {
        application.geoDependencyComponent = DaggerGeoDependencyComponent.builder()
            .application(application)
            .geoDependencyModule(object : GeoDependencyModule() {
                override fun providesGeoPointViewModelFactory(application: Application) =
                    mock<GeoPointViewModelFactory> {
                        on { create(GeoPointViewModel::class.java) } doReturn viewModel
                    }
            })
            .build()
    }

    @Test
    fun `disables save until location is available`() {
        launcherRule.launchDialogFragment(GeoPointDialogFragment::class.java)
        onViewInDialog(withText(R.string.save)).check(matches(not(isEnabled())))

        currentAccuracyLiveData.value = GeoPointAccuracy.Improving(5.0f)
        onViewInDialog(withText(R.string.save)).check(matches(isEnabled()))
    }

    @Test
    fun `shows accuracy threshold`() {
        whenever(viewModel.accuracyThreshold).thenReturn(5.0f)
        launcherRule.launchDialogFragment(GeoPointDialogFragment::class.java)

        onViewInDialog(
            withText(
                application.getLocalizedString(
                    R.string.point_will_be_saved,
                    "5m"
                )
            )
        ).perform(nestedScrollTo()).check(
            matches(isDisplayed())
        )
    }

    @Test
    fun `shows and updates current accuracy`() {
        val scenario = launcherRule.launchDialogFragment(GeoPointDialogFragment::class.java)

        currentAccuracyLiveData.value = GeoPointAccuracy.Improving(50.2f)
        scenario.onFragment {
            assertThat(it.binding.accuracyStatus.accuracy, equalTo(GeoPointAccuracy.Improving(50.2f)))
        }

        currentAccuracyLiveData.value = GeoPointAccuracy.Improving(15.65f)
        onViewInDialog(withText("15.65m")).perform(nestedScrollTo()).check(matches(isDisplayed()))
        scenario.onFragment {
            assertThat(it.binding.accuracyStatus.accuracy, equalTo(GeoPointAccuracy.Improving(15.65f)))
        }
    }

    @Test
    fun `shows and updates time elapsed`() {
        launcherRule.launchDialogFragment(GeoPointDialogFragment::class.java)

        timeElapsedLiveData.value = 0
        onViewInDialog(
            withText(
                application.getLocalizedString(
                    R.string.time_elapsed,
                    "00:00"
                )
            )
        ).perform(nestedScrollTo()).check(
            matches(isDisplayed())
        )

        timeElapsedLiveData.value = 62000
        onViewInDialog(
            withText(
                application.getLocalizedString(
                    R.string.time_elapsed,
                    "01:02"
                )
            )
        ).perform(nestedScrollTo()).check(
            matches(isDisplayed())
        )
    }

    @Test
    fun `shows and updates satellites`() {
        launcherRule.launchDialogFragment(GeoPointDialogFragment::class.java)

        onViewInDialog(withText(application.getLocalizedString(R.string.satellites, 0)))
            .perform(nestedScrollTo())
            .check(matches(isDisplayed()))

        satellitesLiveData.value = 5

        onViewInDialog(withText(application.getLocalizedString(R.string.satellites, 5)))
            .perform(nestedScrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun `clicking cancel calls listener`() {
        val scenario = launcherRule.launchDialogFragment(GeoPointDialogFragment::class.java)

        val listener = mock<GeoPointDialogFragment.Listener>()
        scenario.onFragment {
            it.listener = listener
        }

        onViewInDialog(withText(R.string.cancel)).perform(click())
        verify(listener).onCancel()
    }

    @Test
    fun `pressing back calls listener`() {
        val scenario = launcherRule.launchDialogFragment(GeoPointDialogFragment::class.java)

        val listener = mock<GeoPointDialogFragment.Listener>()
        scenario.onFragment {
            it.listener = listener
        }

        Espresso.pressBack()
        verify(listener).onCancel()
    }

    @Test
    fun `clicking save calls forceLocation() on view model`() {
        launcherRule.launchDialogFragment(GeoPointDialogFragment::class.java)
        currentAccuracyLiveData.value = GeoPointAccuracy.Improving(5.0f)

        onViewInDialog(withText(R.string.save)).perform(click())
        verify(viewModel).forceLocation()
    }

    @Test
    fun `dialog is not cancellable`() {
        launcherRule.launchDialogFragment(GeoPointDialogFragment::class.java).onFragment {
            assertThat(it.isCancelable, equalTo(false))
        }
    }
}
