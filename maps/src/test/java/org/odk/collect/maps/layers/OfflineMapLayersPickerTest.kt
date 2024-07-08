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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView
import org.odk.collect.webpage.ExternalWebPageHelper

@RunWith(AndroidJUnit4::class)
class OfflineMapLayersPickerTest {
    private val referenceLayerRepository = mock<ReferenceLayerRepository>()
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
            assertThat(
                contract,
                instanceOf(ActivityResultContracts.GetMultipleContents()::class.java)
            )
            assertThat(input, equalTo("*/*"))
            dispatchResult(requestCode, uris)
        }
    }

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(OfflineMapLayersPicker::class) {
                OfflineMapLayersPicker(
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
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(ReferenceLayer("1", TempFiles.createTempFile(), "layer1"))
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
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(ReferenceLayer("1", TempFiles.createTempFile(), "layer1"))
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
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(ReferenceLayer("1", TempFiles.createTempFile(), "layer1"))
        )

        launchFragment()

        scheduler.flush()

        Interactions.clickOn(withText("layer1"))
        Interactions.clickOn(withText(string.save))
        assertThat(
            settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER),
            equalTo("1")
        )
    }

    @Test
    fun `when no layer id is saved in settings the 'None' option should be checked`() {
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(ReferenceLayer("1", TempFiles.createTempFile(), "layer1"))
        )

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(
            matches(
                isChecked()
            )
        )
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(
            matches(
                not(isChecked())
            )
        )
    }

    @Test
    fun `when layer id is saved in settings the layer it belongs to should be checked`() {
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", TempFiles.createTempFile(), "layer1"),
                ReferenceLayer("2", TempFiles.createTempFile(), "layer2")
            )
        )

        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, "2")

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(
            matches(
                not(isChecked())
            )
        )
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(
            matches(
                not(isChecked())
            )
        )
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.radio_button)).check(
            matches(
                isChecked()
            )
        )
    }

    @Test
    fun `when layer id is saved in settings but the layer it belongs to does not exist the 'None' option should be checked`() {
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(ReferenceLayer("1", TempFiles.createTempFile(), "layer1"))
        )

        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, "2")

        launchFragment()

        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(2)))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                0,
                R.id.title
            )
        ).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(
            matches(
                isChecked()
            )
        )
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
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                0,
                R.id.title
            )
        ).check(matches(withText(string.none)))
    }

    @Test
    fun `if there are multiple layers all of them are displayed along with the 'None' and sorted in A-Z order`() {
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", TempFiles.createTempFile(), "layerB"),
                ReferenceLayer("2", TempFiles.createTempFile(), "layerA")
            )
        )

        launchFragment()

        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                0,
                R.id.title
            )
        ).check(matches(withText(string.none)))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                1,
                R.id.title
            )
        ).check(matches(withText("layerA")))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                2,
                R.id.title
            )
        ).check(matches(withText("layerB")))
    }

    @Test
    fun `checking layers sets selection correctly`() {
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(ReferenceLayer("1", TempFiles.createTempFile(), "layer1"))
        )

        launchFragment()

        scheduler.flush()

        Interactions.clickOn(withText("layer1"))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(
            matches(
                not(isChecked())
            )
        )
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(
            matches(
                isChecked()
            )
        )

        Interactions.clickOn(withText(string.none))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(
            matches(
                isChecked()
            )
        )
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(
            matches(
                not(isChecked())
            )
        )
    }

    @Test
    fun `recreating maintains selection`() {
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(ReferenceLayer("1", TempFiles.createTempFile(), "layer1"))
        )

        val scenario = launchFragment()

        scheduler.flush()

        Interactions.clickOn(withText("layer1"))
        scenario.recreate()
        scheduler.flush()
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(
            matches(
                not(isChecked())
            )
        )
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(
            matches(
                isChecked()
            )
        )
    }

    @Test
    fun `clicking the 'add layer' and selecting layers displays the confirmation dialog`() {
        val scenario = launchFragment()

        uris.add(Uri.parse("blah"))
        Interactions.clickOn(withText(string.add_layer))

        scenario.onFragment {
            assertThat(
                it.childFragmentManager.findFragmentByTag(OfflineMapLayersImporter::class.java.name),
                instanceOf(OfflineMapLayersImporter::class.java)
            )
        }
    }

    @Test
    fun `clicking the 'add layer' and selecting nothing does not display the confirmation dialog`() {
        val scenario = launchFragment()

        Interactions.clickOn(withText(string.add_layer))

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

        launchFragment()

        scheduler.flush()

        uris.add(file1.toUri())
        uris.add(file2.toUri())

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
        val file1 = TempFiles.createTempFile("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFile("layer2", MbtilesFile.FILE_EXTENSION)

        launchFragment()

        scheduler.flush()

        uris.add(file1.toUri())
        uris.add(file2.toUri())

        Interactions.clickOn(withText(string.add_layer))
        scheduler.flush()
        onView(withId(R.id.add_layer_button)).inRoot(isDialog()).perform(scrollTo(), click())
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", TempFiles.createTempFile(), file1.name),
                ReferenceLayer("2", TempFiles.createTempFile(), file2.name)
            )
        )
        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                0,
                R.id.title
            )
        ).check(matches(withText(string.none)))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                1,
                R.id.title
            )
        ).check(matches(withText(file1.name)))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                2,
                R.id.title
            )
        ).check(matches(withText(file2.name)))
    }

    @Test
    fun `layers are collapsed by default`() {
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", TempFiles.createTempFile(), "layer1"),
                ReferenceLayer("2", TempFiles.createTempFile(), "layer2")
            )
        )

        launchFragment()

        scheduler.flush()

        assertLayerCollapsed(1)
        assertLayerCollapsed(2)
    }

    @Test
    fun `recreating maintains expanded layers`() {
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", TempFiles.createTempFile(), "layer1"),
                ReferenceLayer("2", TempFiles.createTempFile(), "layer2"),
                ReferenceLayer("3", TempFiles.createTempFile(), "layer3")
            )
        )

        val scenario = launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(3))
        onView(withRecyclerView(R.id.layers).atPositionOnView(3, R.id.arrow)).perform(click())

        scenario.recreate()
        scheduler.flush()

        assertLayerExpanded(1)
        assertLayerCollapsed(2)
        assertLayerExpanded(3)
    }

    @Test
    fun `correct path is displayed after expanding layers`() {
        val file1 = TempFiles.createTempFile()
        val file2 = TempFiles.createTempFile()
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", file1, "layer1"),
                ReferenceLayer("2", file2, "layer2")
            )
        )

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.arrow)).perform(click())

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.path)).check(
            matches(
                withText(
                    file1.absolutePath
                )
            )
        )
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.path)).check(
            matches(
                withText(
                    file2.absolutePath
                )
            )
        )
    }

    @Test
    fun `clicking delete shows the confirmation dialog`() {
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
            )
        )

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.delete_layer)).perform(
            scrollTo(),
            click()
        )

        onView(withText(string.cancel)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(string.delete_layer)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun `clicking delete and canceling does not remove the layer`() {
        val layerFile1 = TempFiles.createTempFile()
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", layerFile1, "layer1")
            )
        )

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.delete_layer)).perform(
            scrollTo(),
            click()
        )

        onView(withText(string.cancel)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(2)))
        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                0,
                R.id.title
            )
        ).check(matches(withText(string.none)))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                1,
                R.id.title
            )
        ).check(matches(withText("layer1")))
        verify(referenceLayerRepository, never()).delete("1")
    }

    @Test
    fun `clicking delete and confirming removes the layer`() {
        val layerFile1 = TempFiles.createTempFile()
        val layerFile2 = TempFiles.createTempFile()
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", layerFile1, "layer1"),
                ReferenceLayer("2", layerFile2, "layer2")
            )
        )

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.delete_layer)).perform(
            scrollTo(),
            click()
        )

        onView(withText(string.delete_layer)).inRoot(isDialog()).perform(click())
        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(2)))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                0,
                R.id.title
            )
        ).check(matches(withText(string.none)))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                1,
                R.id.title
            )
        ).check(matches(withText("layer2")))
        verify(referenceLayerRepository).delete("1")
        verify(referenceLayerRepository, never()).delete("2")
    }

    @Test
    fun `deleting the selected layer changes selection to 'none' and saves it`() {
        val layerFile1 = TempFiles.createTempFile()
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", layerFile1, "layer1")
            )
        )
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, "1")

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.delete_layer)).perform(
            scrollTo(),
            click()
        )

        onView(withText(string.delete_layer)).inRoot(isDialog()).perform(click())
        scheduler.flush()

        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                0,
                R.id.title
            )
        ).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(
            matches(
                isChecked()
            )
        )
        assertThat(
            settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER),
            equalTo(null)
        )
    }

    @Test
    fun `deleting one of the layers keeps the list sorted in A-Z order`() {
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", TempFiles.createTempFile(), "layerC"),
                ReferenceLayer("2", TempFiles.createTempFile(), "layerB"),
                ReferenceLayer("3", TempFiles.createTempFile(), "layerA")
            )
        )

        launchFragment()

        scheduler.flush()

        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.delete_layer)).perform(
            scrollTo(),
            click()
        )

        onView(withText(string.delete_layer)).inRoot(isDialog()).perform(click())
        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                0,
                R.id.title
            )
        ).check(matches(withText(string.none)))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                1,
                R.id.title
            )
        ).check(matches(withText("layerA")))
        onView(
            withRecyclerView(R.id.layers).atPositionOnView(
                2,
                R.id.title
            )
        ).check(matches(withText("layerC")))
    }

    @Test
    fun `progress indicator is displayed during deleting layers`() {
        val layerFile1 = TempFiles.createTempFile("layer1", MbtilesFile.FILE_EXTENSION)
        whenever(referenceLayerRepository.getAll()).thenReturn(
            listOf(
                ReferenceLayer("1", layerFile1, "layer1"),
            )
        )

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.delete_layer)).perform(
            scrollTo(),
            click()
        )
        onView(withText(string.delete_layer)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.progress_indicator)).check(matches(isDisplayed()))
        onView(withId(R.id.layers)).check(matches(not(isDisplayed())))

        scheduler.flush()

        onView(withId(R.id.progress_indicator)).check(matches(not(isDisplayed())))
        onView(withId(R.id.layers)).check(matches(isDisplayed()))
    }

    private fun assertLayerCollapsed(position: Int) {
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.arrow)).check(
            matches(
                withImageDrawable(org.odk.collect.icons.R.drawable.ic_baseline_expand_24)
            )
        )
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.path)).check(
            matches(
                withEffectiveVisibility(ViewMatchers.Visibility.GONE)
            )
        )
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.delete_layer)).check(
            matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE))
        )
    }

    private fun assertLayerExpanded(position: Int) {
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.arrow)).check(
            matches(
                withImageDrawable(org.odk.collect.icons.R.drawable.ic_baseline_collapse_24)
            )
        )
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.path)).check(
            matches(
                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            )
        )
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.delete_layer)).check(
            matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        )
    }

    @Test
    fun `the confirmation dialog is dismissed o activity recreation`() {
        val scenario = launchFragment()

        uris.add(Uri.parse("blah"))
        Interactions.clickOn(withText(string.add_layer))

        scenario.onFragment {
            assertThat(
                it.childFragmentManager.findFragmentByTag(OfflineMapLayersImporter::class.java.name),
                instanceOf(OfflineMapLayersImporter::class.java)
            )
        }

        scenario.recreate()

        scenario.onFragment {
            assertThat(
                it.childFragmentManager.findFragmentByTag(OfflineMapLayersImporter::class.java.name),
                equalTo(null)
            )
        }
    }

    private fun launchFragment(): FragmentScenario<OfflineMapLayersPicker> {
        return fragmentScenarioLauncherRule.launchInContainer(OfflineMapLayersPicker::class.java)
    }
}
