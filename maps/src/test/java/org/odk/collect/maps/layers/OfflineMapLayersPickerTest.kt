package org.odk.collect.maps.layers

import android.net.Uri
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.maps.R
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView
import org.odk.collect.webpage.ExternalWebPageHelper

@RunWith(AndroidJUnit4::class)
class OfflineMapLayersPickerTest {
    private val layers = listOf(
        ReferenceLayer("1", TempFiles.createTempFile(), "layer1"),
        ReferenceLayer("2", TempFiles.createTempFile(), "layer2")
    )
    private val referenceLayerRepository = mock<ReferenceLayerRepository>().also {
        whenever(it.getAllSupported()).thenReturn(emptyList())
    }
    private val scheduler = FakeScheduler()
    private val settingsProvider = InMemSettingsProvider()
    private val viewModelFactory = OfflineMapLayersPickerViewModel.Factory(referenceLayerRepository, scheduler, settingsProvider)
    private val externalWebPageHelper = mock<ExternalWebPageHelper>()

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(OfflineMapLayersPicker::class) {
                OfflineMapLayersPicker(viewModelFactory, externalWebPageHelper)
            }.build()
    )

    @Test
    fun `clicking the 'cancel' button dismisses the layers picker`() {
        val scenario = launchFragment()

        scheduler.flush()

        scenario.onFragment {
            assertThat(it.isVisible, equalTo(true))
            onView(withText(string.cancel)).perform(click())
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `selecting a new layer and clicking the 'cancel' button does not save the layer`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(layers)

        launchFragment()

        scheduler.flush()

        onView(withText("layer2")).perform(click())
        onView(withText(string.cancel)).perform(click())
        assertThat(settingsProvider.getUnprotectedSettings().contains(ProjectKeys.KEY_REFERENCE_LAYER), equalTo(false))
    }

    @Test
    fun `clicking the 'save' button dismisses the layers picker`() {
        val scenario = launchFragment()

        scheduler.flush()

        scenario.onFragment {
            assertThat(it.isVisible, equalTo(true))
            onView(withText(string.save)).perform(click())
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `clicking the 'save' button saves null when none of the layers is selected`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(layers)

        launchFragment()

        scheduler.flush()

        onView(withText(string.save)).perform(click())
        assertThat(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER), equalTo(null))
    }

    @Test
    fun `clicking the 'save' button saves the current layer`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(layers)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, "2")

        launchFragment()

        scheduler.flush()

        onView(withText(string.save)).perform(click())
        assertThat(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER), equalTo("2"))
    }

    @Test
    fun `selecting a new layer and clicking the 'save' button saves the new layer`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(layers)

        launchFragment()

        scheduler.flush()

        onView(withText("layer2")).perform(click())
        onView(withText(string.save)).perform(click())
        assertThat(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER), equalTo("2"))
    }

    @Test
    fun `selecting 'none' and clicking the 'save' button saves null`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(layers)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, "2")

        launchFragment()

        scheduler.flush()

        onView(withText(string.none)).perform(click())
        onView(withText(string.save)).perform(click())
        assertThat(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER), equalTo(null))
    }

    @Test
    fun `progress indicator is displayed during loading layers`() {
        launchFragment()

        onView(withId(R.id.progress_indicator)).check(matches(isDisplayed()))
        onView(withId(R.id.layers)).check(matches(not(isDisplayed())))

        scheduler.flush()

        onView(withId(R.id.progress_indicator)).check(matches(not(isDisplayed())))
        onView(withId(R.id.layers)).check(matches(isDisplayed()))
    }

    @Test
    fun `clicking the 'learn more' button opens the forum thread`() {
        launchFragment()

        scheduler.flush()

        onView(withText(string.get_help_with_reference_layers)).perform(click())

        verify(externalWebPageHelper).openWebPageInCustomTab(any(), eq(Uri.parse("https://docs.getodk.org/collect-offline-maps/#transferring-offline-tilesets-to-devices")))
    }

    @Test
    fun `if there are no layers the 'none' option is displayed`() {
        launchFragment()

        scheduler.flush()

        onView(withText(string.none)).check(matches(isDisplayed()))
    }

    @Test
    fun `if there are multiple layers all of them are displayed along with the 'none' option`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(layers)

        launchFragment()

        scheduler.flush()

        onView(withText(string.none)).check(matches(isDisplayed()))
        onView(withText("layer1")).check(matches(isDisplayed()))
        onView(withText("layer2")).check(matches(isDisplayed()))
    }

    @Test
    fun `recreating maintains selection`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(layers)

        val scenario = launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(isChecked()))
        onView(withText("layer2")).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.radio_button)).check(matches(isChecked()))
        scenario.recreate()
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.radio_button)).check(matches(isChecked()))
    }

    private fun launchFragment(): FragmentScenario<OfflineMapLayersPicker> {
        return fragmentScenarioLauncherRule.launchInContainer(OfflineMapLayersPicker::class.java)
    }
}
