package org.odk.collect.maps.layers

import android.net.Uri
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toUri
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.maps.R
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.EspressoHelpers
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.RecyclerViewMatcher
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView
import org.odk.collect.webpage.ExternalWebPageHelper

@RunWith(AndroidJUnit4::class)
class OfflineMapLayersPickerTest {
    private val referenceLayerRepository = TestReferenceLayerRepository()
    private val scheduler = FakeScheduler()
    private val settingsProvider = InMemSettingsProvider()
    private val externalWebPageHelper = mock<ExternalWebPageHelper>()

    private val uris = mutableListOf<Uri>()
    private val testRegistry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            assertThat(contract, instanceOf(ActivityResultContracts.GetMultipleContents()::class.java))
            assertThat(input, equalTo("*/*"))
            dispatchResult(requestCode, uris)
        }
    }

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(OfflineMapLayersPicker::class) {
                OfflineMapLayersPicker(testRegistry, referenceLayerRepository, scheduler, settingsProvider, externalWebPageHelper)
            }.build()
    )

    @Test
    fun `clicking the 'cancel' button dismisses the layers picker`() {
        val scenario = launchOfflineMapLayersPicker()

        scenario.onFragment {
            assertThat(it.isVisible, equalTo(true))
            EspressoHelpers.clickOnText(string.cancel)
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `clicking the 'cancel' button does not save the layer`() {
        referenceLayerRepository.addLayers(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        )

        launchOfflineMapLayersPicker()

        scheduler.flush()

        EspressoHelpers.clickOnText(string.cancel)
        assertThat(settingsProvider.getUnprotectedSettings().contains(ProjectKeys.KEY_REFERENCE_LAYER), equalTo(false))
    }

    @Test
    fun `the 'cancel' button should be enabled during loading layers`() {
        launchOfflineMapLayersPicker()

        onView(withText(string.cancel)).check(matches(isEnabled()))
    }

    @Test
    fun `clicking the 'save' button dismisses the layers picker`() {
        val scenario = launchOfflineMapLayersPicker()

        scheduler.flush()

        scenario.onFragment {
            assertThat(it.isVisible, equalTo(true))
            EspressoHelpers.clickOnText(string.save)
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `the 'save' button should be disabled during loading layers`() {
        launchOfflineMapLayersPicker()

        onView(withText(string.save)).check(matches(not(isEnabled())))
        scheduler.flush()
        onView(withText(string.save)).check(matches(isEnabled()))
    }

    @Test
    fun `clicking the 'save' button saves null when 'None' option is checked`() {
        referenceLayerRepository.addLayers(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        )

        launchOfflineMapLayersPicker()

        scheduler.flush()

        EspressoHelpers.clickOnText(string.save)
        assertThat(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER), equalTo(null))
    }

    @Test
    fun `clicking the 'save' button saves the layer id if any is checked`() {
        referenceLayerRepository.addLayers(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        )

        launchOfflineMapLayersPicker()

        scheduler.flush()

        EspressoHelpers.clickOnText("layer1")
        EspressoHelpers.clickOnText(string.save)
        assertThat(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER), equalTo("1"))
    }

    @Test
    fun `when no layer id is saved in settings the 'None' option should be checked`() {
        referenceLayerRepository.addLayers(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        )

        launchOfflineMapLayersPicker()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(isChecked()))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(not(isChecked())))
    }

    @Test
    fun `when layer id is saved in settings the layer it belongs to should be checked`() {
        referenceLayerRepository.addLayers(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1"),
            ReferenceLayer("2", TempFiles.createTempFile(), "layer2")
        )

        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, "2")

        launchOfflineMapLayersPicker()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(not(isChecked())))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(not(isChecked())))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.radio_button)).check(matches(isChecked()))
    }

    @Test
    fun `progress indicator is displayed during loading layers`() {
        launchOfflineMapLayersPicker()

        onView(withId(R.id.progress_indicator)).check(matches(isDisplayed()))
        onView(withId(R.id.layers)).check(matches(not(isDisplayed())))

        scheduler.flush()

        onView(withId(R.id.progress_indicator)).check(matches(not(isDisplayed())))
        onView(withId(R.id.layers)).check(matches(isDisplayed()))
    }

    @Test
    fun `the 'learn more' button should be enabled during loading layers`() {
        launchOfflineMapLayersPicker()

        onView(withText(string.get_help_with_reference_layers)).check(matches(isEnabled()))
    }

    @Test
    fun `clicking the 'learn more' button opens the forum thread`() {
        launchOfflineMapLayersPicker()

        scheduler.flush()

        EspressoHelpers.clickOnText(string.get_help_with_reference_layers)

        verify(externalWebPageHelper).openWebPageInCustomTab(any(), eq(Uri.parse("https://docs.getodk.org/collect-offline-maps/#transferring-offline-tilesets-to-devices")))
    }

    @Test
    fun `if there are no layers the 'none' option is displayed`() {
        launchOfflineMapLayersPicker()

        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(1)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(withText(string.none)))
    }

    @Test
    fun `if there are multiple layers all of them are displayed along with the 'None'`() {
        referenceLayerRepository.addLayers(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1"),
            ReferenceLayer("2", TempFiles.createTempFile(), "layer2")
        )

        launchOfflineMapLayersPicker()

        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(withText("layer1")))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.radio_button)).check(matches(withText("layer2")))
    }

    @Test
    fun `checking layers sets selection correctly`() {
        referenceLayerRepository.addLayers(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        )

        launchOfflineMapLayersPicker()

        scheduler.flush()

        EspressoHelpers.clickOnText("layer1")
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(not(isChecked())))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(isChecked()))

        EspressoHelpers.clickOnText(string.none)
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(isChecked()))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(not(isChecked())))
    }

    @Test
    fun `recreating maintains selection`() {
        referenceLayerRepository.addLayers(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        )

        val scenario = launchOfflineMapLayersPicker()

        scheduler.flush()

        EspressoHelpers.clickOnText("layer1")
        scenario.recreate()
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(not(isChecked())))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(isChecked()))
    }

    @Test
    fun `clicking the 'add layer' and selecting layers displays the confirmation dialog`() {
        val scenario = launchOfflineMapLayersPicker()

        uris.add(Uri.parse("blah"))
        EspressoHelpers.clickOnText(string.add_layer)

        scenario.onFragment {
            assertThat(
                it.childFragmentManager.findFragmentByTag(OfflineMapLayersImporter::class.java.name),
                instanceOf(OfflineMapLayersImporter::class.java)
            )
        }
    }

    @Test
    fun `clicking the 'add layer' and selecting nothing does not display the confirmation dialog`() {
        val scenario = launchOfflineMapLayersPicker()

        EspressoHelpers.clickOnText(string.add_layer)

        scenario.onFragment {
            assertThat(
                it.childFragmentManager.findFragmentByTag(OfflineMapLayersImporter::class.java.name),
                equalTo(null)
            )
        }
    }

    @Test
    fun `progress indicator is displayed during loading layers after receiving new ones`() {
        val file1 = TempFiles.createTempFile("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFile("layer2", MbtilesFile.FILE_EXTENSION)

        launchOfflineMapLayersPicker()

        scheduler.flush()

        uris.add(file1.toUri())
        uris.add(file2.toUri())

        EspressoHelpers.clickOnText(string.add_layer)
        scheduler.flush()
        onView(withId(R.id.add_layer_button)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.progress_indicator)).check(matches(isDisplayed()))
        onView(withId(R.id.layers)).check(matches(not(isDisplayed())))

        scheduler.flush()

        onView(withId(R.id.progress_indicator)).check(matches(not(isDisplayed())))
        onView(withId(R.id.layers)).check(matches(isDisplayed()))
    }

    @Test
    fun `when new layers added the list should be updated`() {
        val file1 = TempFiles.createTempFile("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFile("layer2", MbtilesFile.FILE_EXTENSION)

        launchOfflineMapLayersPicker()

        scheduler.flush()

        uris.add(file1.toUri())
        uris.add(file2.toUri())

        EspressoHelpers.clickOnText(string.add_layer)
        scheduler.flush()
        onView(withId(R.id.add_layer_button)).inRoot(isDialog()).perform(click())
        referenceLayerRepository.addLayers(
            ReferenceLayer("1", file1, file1.name),
            ReferenceLayer("2", file2, file2.name)
        )
        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(withText(file1.name)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.radio_button)).check(matches(withText(file2.name)))
    }

    private fun launchOfflineMapLayersPicker(): FragmentScenario<OfflineMapLayersPicker> {
        return fragmentScenarioLauncherRule.launchInContainer(OfflineMapLayersPicker::class.java)
    }
}
