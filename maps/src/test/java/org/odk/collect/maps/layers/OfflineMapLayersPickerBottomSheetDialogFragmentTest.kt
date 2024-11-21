package org.odk.collect.maps.layers

import android.net.Uri
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toUri
import androidx.fragment.app.testing.FragmentScenario
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
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
import org.odk.collect.androidtest.DrawableMatcher.withImageDrawable
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.maps.R
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.Interactions
import org.odk.collect.testshared.RecyclerViewMatcher
import org.odk.collect.testshared.ViewMatchers.atPositionInRecyclerView
import org.odk.collect.testshared.WaitFor
import org.odk.collect.webpage.ExternalWebPageHelper

@RunWith(AndroidJUnit4::class)
class OfflineMapLayersPickerBottomSheetDialogFragmentTest {
    private val referenceLayerRepository = InMemReferenceLayerRepository()
    private val scheduler = FakeScheduler()
    private val settingsProvider = InMemSettingsProvider()
    private val externalWebPageHelper = mock<ExternalWebPageHelper>()

    private val testRegistry = TestRegistry()

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(OfflineMapLayersPickerBottomSheetDialogFragment::class) {
                OfflineMapLayersPickerBottomSheetDialogFragment(
                    testRegistry,
                    referenceLayerRepository,
                    scheduler,
                    settingsProvider,
                    externalWebPageHelper
                )
            }.build()
    )

    @Test
    fun `clicking the 'cancel' button dismisses the layers picker`() {
        val scenario = launchFragment()

        scenario.onFragment {
            assertThat(it.isVisible, equalTo(true))
            Interactions.clickOn(withText(string.cancel))
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `clicking the 'cancel' button does not save the layer`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )

        launchFragment()

        scheduler.flush()

        Interactions.clickOn(withText(string.cancel))
        assertThat(
            settingsProvider.getUnprotectedSettings().contains(ProjectKeys.KEY_REFERENCE_LAYER),
            equalTo(false)
        )
    }

    @Test
    fun `the 'cancel' button should be enabled during loading layers`() {
        launchFragment()

        onView(withText(string.cancel)).check(matches(isEnabled()))
    }

    @Test
    fun `clicking the 'save' button dismisses the layers picker`() {
        val scenario = launchFragment()

        scheduler.flush()

        scenario.onFragment {
            assertThat(it.isVisible, equalTo(true))
            Interactions.clickOn(withText(string.save))
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `the 'save' button should be disabled during loading layers`() {
        launchFragment()

        onView(withText(string.save)).check(matches(not(isEnabled())))
        scheduler.flush()
        onView(withText(string.save)).check(matches(isEnabled()))
    }

    @Test
    fun `clicking the 'save' button saves null when 'None' option is checked`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )

        launchFragment()

        scheduler.flush()

        Interactions.clickOn(withText(string.save))
        assertThat(
            settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER),
            equalTo(null)
        )
    }

    @Test
    fun `clicking the 'save' button saves the layer id if any is checked`() {
        val file = TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION)
        referenceLayerRepository.addLayer(file, true)

        launchFragment()

        scheduler.flush()

        Interactions.clickOn(withText("layer1${MbtilesFile.FILE_EXTENSION}"))
        Interactions.clickOn(withText(string.save))
        assertThat(
            settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER),
            equalTo(file.absolutePath)
        )
    }

    @Test
    fun `when no layer id is saved in settings the 'None' option should be checked`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )

        launchFragment()

        scheduler.flush()

        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.radio_button))
            .check(matches(isChecked()))

        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.radio_button))
            .check(matches(not(isChecked())))
    }

    @Test
    fun `when layer id is saved in settings the layer it belongs to should be checked`() {
        val file1 = TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFileWithName("layer2", MbtilesFile.FILE_EXTENSION)
        referenceLayerRepository.addLayer(file1, true)
        referenceLayerRepository.addLayer(file2, true)

        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, file2.absolutePath)

        launchFragment()

        scheduler.flush()

        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.radio_button))
            .check(matches(not(isChecked())))

        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.radio_button))
            .check(matches(not(isChecked())))

        onView(atPositionInRecyclerView(R.id.layers, 2, R.id.radio_button))
            .check(matches(isChecked()))
    }

    @Test
    fun `when layer id is saved in settings but the layer it belongs to does not exist the 'None' option should be checked`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )

        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, "2")

        launchFragment()

        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(2)))

        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.title))
            .check(matches(withText(string.none)))

        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.radio_button))
            .check(matches(isChecked()))
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
    fun `the 'learn more' button should be enabled during loading layers`() {
        launchFragment()

        onView(withText(string.get_help_with_offline_layers)).check(matches(isEnabled()))
    }

    @Test
    fun `clicking the 'learn more' button opens the forum thread`() {
        launchFragment()

        scheduler.flush()

        Interactions.clickOn(withText(string.get_help_with_offline_layers))

        verify(externalWebPageHelper).openWebPageInCustomTab(
            any(),
            eq(Uri.parse("https://docs.getodk.org/collect-offline-maps/#transferring-offline-tilesets-to-devices"))
        )
    }

    @Test
    fun `if there are no layers the 'none' option is displayed`() {
        launchFragment()

        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(1)))

        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.title))
            .check(matches(withText(string.none)))
    }

    @Test
    fun `if there are multiple layers all of them are displayed along with the 'None' and sorted in A-Z order`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layerB", MbtilesFile.FILE_EXTENSION), true
        )
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layerA", MbtilesFile.FILE_EXTENSION), true
        )

        launchFragment()

        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))

        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.title))
            .check(matches(withText(string.none)))

        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.title))
            .check(matches(withText("layerA${MbtilesFile.FILE_EXTENSION}")))

        onView(atPositionInRecyclerView(R.id.layers, 2, R.id.title))
            .check(matches(withText("layerB${MbtilesFile.FILE_EXTENSION}")))
    }

    @Test
    fun `checking layers sets selection correctly`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )

        launchFragment()

        scheduler.flush()

        Interactions.clickOn(withText("layer1${MbtilesFile.FILE_EXTENSION}"))
        WaitFor.waitFor {
            onView(atPositionInRecyclerView(R.id.layers, 0, R.id.radio_button))
                .check(matches(not(isChecked())))

            onView(atPositionInRecyclerView(R.id.layers, 1, R.id.radio_button))
                .check(matches(isChecked()))
        }

        Interactions.clickOn(withText(string.none))
        WaitFor.waitFor {
            onView(atPositionInRecyclerView(R.id.layers, 0, R.id.radio_button))
                .check(matches(isChecked()))

            onView(atPositionInRecyclerView(R.id.layers, 1, R.id.radio_button))
                .check(matches(not(isChecked())))
        }
    }

    @Test
    fun `recreating maintains selection`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )

        val scenario = launchFragment()

        scheduler.flush()

        Interactions.clickOn(withText("layer1${MbtilesFile.FILE_EXTENSION}"))
        scenario.recreate()
        scheduler.flush()
        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.radio_button))
            .check(matches(not(isChecked())))

        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.radio_button))
            .check(matches(isChecked()))
    }

    @Test
    fun `clicking the 'add layer' and selecting layers displays the confirmation dialog`() {
        val scenario = launchFragment()

        testRegistry.addUris(Uri.parse("blah"))
        Interactions.clickOn(withText(string.add_layer))

        scenario.onFragment {
            assertThat(
                it.childFragmentManager.findFragmentByTag(OfflineMapLayersImporterDialogFragment::class.java.name),
                instanceOf(OfflineMapLayersImporterDialogFragment::class.java)
            )
        }
    }

    @Test
    fun `clicking the 'add layer' and selecting nothing does not display the confirmation dialog`() {
        val scenario = launchFragment()

        Interactions.clickOn(withText(string.add_layer))

        scenario.onFragment {
            assertThat(
                it.childFragmentManager.findFragmentByTag(OfflineMapLayersImporterDialogFragment::class.java.name),
                equalTo(null)
            )
        }
    }

    @Test
    fun `progress indicator is displayed during loading layers after receiving new ones`() {
        val file1 = TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFileWithName("layer2", MbtilesFile.FILE_EXTENSION)

        launchFragment()

        scheduler.flush()

        testRegistry.addUris(file1.toUri(), file2.toUri())

        Interactions.clickOn(withText(string.add_layer))
        scheduler.flush()
        onView(withId(R.id.add_layer_button)).inRoot(isDialog()).perform(scrollTo(), click())

        onView(withId(R.id.progress_indicator)).check(matches(isDisplayed()))
        onView(withId(R.id.layers)).check(matches(not(isDisplayed())))

        scheduler.flush()

        onView(withId(R.id.progress_indicator)).check(matches(not(isDisplayed())))
        onView(withId(R.id.layers)).check(matches(isDisplayed()))
    }

    @Test
    fun `when new layers added the list should be updated`() {
        val file1 = TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFileWithName("layer2", MbtilesFile.FILE_EXTENSION)

        launchFragment()
        scheduler.flush()
        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(1)))

        testRegistry.addUris(file1.toUri(), file2.toUri())

        Interactions.clickOn(withText(string.add_layer))
        scheduler.flush()
        onView(withId(R.id.add_layer_button)).inRoot(isDialog()).perform(scrollTo(), click())
        scheduler.flush()

        WaitFor.waitFor {
            onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))
        }

        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.title))
            .check(matches(withText(string.none)))

        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.title))
            .check(matches(withText(file1.name)))

        onView(atPositionInRecyclerView(R.id.layers, 2, R.id.title))
            .check(matches(withText(file2.name)))
    }

    @Test
    fun `layers are collapsed by default`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer2", MbtilesFile.FILE_EXTENSION), true
        )

        launchFragment()

        scheduler.flush()

        assertLayerCollapsed(1)
        assertLayerCollapsed(2)
    }

    @Test
    fun `recreating maintains expanded layers`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer2", MbtilesFile.FILE_EXTENSION), true
        )
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer3", MbtilesFile.FILE_EXTENSION), true
        )

        val scenario = launchFragment()

        scheduler.flush()

        expandLayer(1)
        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(3))
        expandLayer(3)

        scenario.recreate()
        scheduler.flush()

        assertLayerExpanded(1)
        assertLayerCollapsed(2)
        assertLayerExpanded(3)
    }

    @Test
    fun `correct path is displayed after expanding layers`() {
        val file1 = TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFileWithName("layer2", MbtilesFile.FILE_EXTENSION)
        referenceLayerRepository.addLayer(file1, true)
        referenceLayerRepository.addLayer(file2, true)

        launchFragment()

        scheduler.flush()

        expandLayer(1)
        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
        expandLayer(2)

        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.path))
            .check(matches(withText(file1.absolutePath)))

        onView(atPositionInRecyclerView(R.id.layers, 2, R.id.path))
            .check(matches(withText(file2.absolutePath)))
    }

    @Test
    fun `clicking delete shows the confirmation dialog`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )

        launchFragment()

        scheduler.flush()

        expandLayer(1)
        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.delete_layer))
            .perform(scrollTo(), click())

        onView(withText(string.cancel)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(string.delete_layer)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun `clicking delete and canceling does not remove the layer`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )

        launchFragment()

        scheduler.flush()

        expandLayer(1)
        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.delete_layer))
            .perform(scrollTo(), click())

        onView(withText(string.cancel)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(2)))
        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.title))
            .check(matches(withText(string.none)))
        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.title))
            .check(matches(withText("layer1${MbtilesFile.FILE_EXTENSION}")))
        assertThat(referenceLayerRepository.getAll().size, equalTo(1))
    }

    @Test
    fun `clicking delete and confirming removes the layer`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer2", MbtilesFile.FILE_EXTENSION), true
        )

        launchFragment()

        scheduler.flush()

        expandLayer(1)
        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.delete_layer))
            .perform(scrollTo(), click())

        onView(withText(string.delete_layer)).inRoot(isDialog()).perform(click())
        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(2)))
        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.title))
            .check(matches(withText(string.none)))
        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.title))
            .check(matches(withText("layer2${MbtilesFile.FILE_EXTENSION}")))

        assertThat(referenceLayerRepository.getAll().size, equalTo(1))
    }

    @Test
    fun `deleting the selected layer changes selection to 'none' and saves it`() {
        val file = TempFiles.createTempFileWithName("layer1.${MbtilesFile.FILE_EXTENSION}", MbtilesFile.FILE_EXTENSION)
        referenceLayerRepository.addLayer(file, true)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, file.absolutePath)

        launchFragment()

        scheduler.flush()

        expandLayer(1)
        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.delete_layer))
            .perform(scrollTo(), click())

        onView(withText(string.delete_layer)).inRoot(isDialog()).perform(click())
        scheduler.flush()

        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.title))
            .check(matches(withText(string.none)))
        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.radio_button))
            .check(matches(isChecked()))
        assertThat(
            settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER),
            equalTo(null)
        )
    }

    @Test
    fun `deleting one of the layers keeps the list sorted in A-Z order`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layerC", MbtilesFile.FILE_EXTENSION), true
        )
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layerB", MbtilesFile.FILE_EXTENSION), true
        )
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layerA", MbtilesFile.FILE_EXTENSION), true
        )

        launchFragment()

        scheduler.flush()

        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
        expandLayer(2)
        onView(atPositionInRecyclerView(R.id.layers, 2, R.id.delete_layer))
            .perform(scrollTo(), click())

        onView(withText(string.delete_layer)).inRoot(isDialog()).perform(click())
        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))
        onView(atPositionInRecyclerView(R.id.layers, 0, R.id.title))
            .check(matches(withText(string.none)))
        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.title))
            .check(matches(withText("layerA${MbtilesFile.FILE_EXTENSION}")))
        onView(atPositionInRecyclerView(R.id.layers, 2, R.id.title))
            .check(matches(withText("layerC${MbtilesFile.FILE_EXTENSION}")))
    }

    @Test
    fun `progress indicator is displayed during deleting layers`() {
        referenceLayerRepository.addLayer(
            TempFiles.createTempFileWithName("layer1", MbtilesFile.FILE_EXTENSION), true
        )

        launchFragment()

        scheduler.flush()

        expandLayer(1)
        onView(atPositionInRecyclerView(R.id.layers, 1, R.id.delete_layer))
            .perform(scrollTo(), click())
        onView(withText(string.delete_layer)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.progress_indicator)).check(matches(isDisplayed()))
        onView(withId(R.id.layers)).check(matches(not(isDisplayed())))

        scheduler.flush()

        onView(withId(R.id.progress_indicator)).check(matches(not(isDisplayed())))
        onView(withId(R.id.layers)).check(matches(isDisplayed()))
    }

    @Test
    fun `the confirmation dialog is dismissed o activity recreation`() {
        val scenario = launchFragment()

        testRegistry.addUris(Uri.parse("blah"))
        Interactions.clickOn(withText(string.add_layer))

        scenario.onFragment {
            assertThat(
                it.childFragmentManager.findFragmentByTag(OfflineMapLayersImporterDialogFragment::class.java.name),
                instanceOf(OfflineMapLayersImporterDialogFragment::class.java)
            )
        }

        scenario.recreate()

        scenario.onFragment {
            assertThat(
                it.childFragmentManager.findFragmentByTag(OfflineMapLayersImporterDialogFragment::class.java.name),
                equalTo(null)
            )
        }
    }

    private fun expandLayer(position: Int) {
        onView(atPositionInRecyclerView(R.id.layers, position, R.id.arrow))
            .perform(click())
        WaitFor.waitFor {
            assertLayerExpanded(position)
        }
    }

    private fun assertLayerCollapsed(position: Int) {
        onView(atPositionInRecyclerView(R.id.layers, position, R.id.arrow))
            .check(
                matches(
                    withImageDrawable(org.odk.collect.icons.R.drawable.ic_baseline_expand_24)
                )
            )
        onView(atPositionInRecyclerView(R.id.layers, position, R.id.path))
            .check(
                matches(
                    withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                )
            )
        onView(atPositionInRecyclerView(R.id.layers, position, R.id.delete_layer))
            .check(
                matches(
                    withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                )
            )
    }

    private fun assertLayerExpanded(position: Int) {
        onView(atPositionInRecyclerView(R.id.layers, position, R.id.arrow))
            .check(
                matches(
                    withImageDrawable(org.odk.collect.icons.R.drawable.ic_baseline_collapse_24)
                )
            )

        onView(atPositionInRecyclerView(R.id.layers, position, R.id.path))
            .check(
                matches(
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )

        onView(atPositionInRecyclerView(R.id.layers, position, R.id.delete_layer))
            .check(
                matches(
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )
    }

    private fun launchFragment(): FragmentScenario<OfflineMapLayersPickerBottomSheetDialogFragment> {
        return fragmentScenarioLauncherRule.launchInContainer(
            OfflineMapLayersPickerBottomSheetDialogFragment::class.java
        )
    }

    private class TestRegistry : ActivityResultRegistry() {
        val uris = mutableListOf<Uri>()

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            assertThat(
                contract,
                instanceOf(ActivityResultContracts.GetMultipleContents()::class.java)
            )
            assertThat(input, equalTo("*/*"))
            dispatchResult(requestCode, uris)
        }

        fun addUris(vararg uris: Uri) {
            this.uris.addAll(uris)
        }
    }
}
