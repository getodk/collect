package org.odk.collect.maps.layers

import android.app.Application
import androidx.core.net.toUri
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R
import org.odk.collect.strings.localization.getLocalizedQuantityString
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.Interactions
import org.odk.collect.testshared.RecyclerViewMatcher
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class OfflineMapLayersImporterDialogFragmentTest {
    private val scheduler = FakeScheduler()
    private val referenceLayerRepository = InMemReferenceLayerRepository()
    private val settingsProvider = InMemSettingsProvider()

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(OfflineMapLayersImporterDialogFragment::class) {
                OfflineMapLayersImporterDialogFragment(
                    referenceLayerRepository,
                    scheduler,
                    settingsProvider
                )
            }.build()
    )

    @Test
    fun `clicking the 'cancel' button dismisses the dialog`() {
        launchFragment().onFragment {
            scheduler.flush()
            assertThat(it.isVisible, equalTo(true))
            Interactions.clickOn(withText(R.string.cancel))
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `clicking the 'add layer' button dismisses the dialog`() {
        launchFragment().onFragment {
            scheduler.flush()
            assertThat(it.isVisible, equalTo(true))
            it.viewModel.loadLayersToImport(emptyList(), it.requireContext())
            Interactions.clickOn(withId(org.odk.collect.maps.R.id.add_layer_button))
            scheduler.flush()
            RobolectricHelpers.runLooper()
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `progress indicator is displayed during loading layers`() {
        val file1 = TempFiles.createTempFile("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFile("layer2", MbtilesFile.FILE_EXTENSION)

        launchFragment().onFragment {
            it.viewModel.loadLayersToImport(
                listOf(file1.toUri(), file2.toUri()),
                it.requireContext()
            )
        }

        onView(withId(org.odk.collect.maps.R.id.progress_indicator)).check(matches(isDisplayed()))
        onView(withId(org.odk.collect.maps.R.id.layers)).check(matches(not(isDisplayed())))

        scheduler.flush()

        onView(withId(org.odk.collect.maps.R.id.progress_indicator)).check(matches(not(isDisplayed())))
        onView(withId(org.odk.collect.maps.R.id.layers)).check(matches(isDisplayed()))
    }

    @Test
    fun `the 'cancel' button is enabled during loading layers`() {
        launchFragment()

        onView(withId(org.odk.collect.maps.R.id.cancel_button)).check(matches(isEnabled()))
        scheduler.flush()
        onView(withId(org.odk.collect.maps.R.id.cancel_button)).check(matches(isEnabled()))
    }

    @Test
    fun `the 'add layer' button is disabled during loading layers`() {
        val file = TempFiles.createTempFile("layer", MbtilesFile.FILE_EXTENSION)

        launchFragment().onFragment {
            it.viewModel.loadLayersToImport(listOf(file.toUri()), it.requireContext())
        }

        onView(withId(org.odk.collect.maps.R.id.add_layer_button)).check(matches(not(isEnabled())))
        scheduler.flush()
        onView(withId(org.odk.collect.maps.R.id.add_layer_button)).check(matches(isEnabled()))
    }

    @Test
    fun `'All projects' location should be selected by default`() {
        launchFragment()

        onView(withId(org.odk.collect.maps.R.id.all_projects_option)).check(matches(isChecked()))
        onView(withId(org.odk.collect.maps.R.id.current_project_option)).check(matches(not(isChecked())))
    }

    @Test
    fun `checking location sets selection correctly`() {
        launchFragment()
        scheduler.flush()

        Interactions.clickOn(withId(org.odk.collect.maps.R.id.current_project_option))

        onView(withId(org.odk.collect.maps.R.id.all_projects_option)).check(matches(not(isChecked())))
        onView(withId(org.odk.collect.maps.R.id.current_project_option)).check(matches(isChecked()))

        Interactions.clickOn(withId(org.odk.collect.maps.R.id.all_projects_option))

        onView(withId(org.odk.collect.maps.R.id.all_projects_option)).check(matches(isChecked()))
        onView(withId(org.odk.collect.maps.R.id.current_project_option)).check(matches(not(isChecked())))
    }

    @Test
    fun `recreating maintains the selected layers location`() {
        val scenario = launchFragment()
        scheduler.flush()

        Interactions.clickOn(withId(org.odk.collect.maps.R.id.current_project_option))

        scenario.recreate()

        onView(withId(org.odk.collect.maps.R.id.all_projects_option)).check(matches(not(isChecked())))
        onView(withId(org.odk.collect.maps.R.id.current_project_option)).check(matches(isChecked()))
    }

    @Test
    fun `the list of selected layers should be displayed in A-Z order`() {
        val file1 = TempFiles.createTempFile("layerB", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFile("layerA", MbtilesFile.FILE_EXTENSION)

        launchFragment().onFragment {
            it.viewModel.loadLayersToImport(
                listOf(file1.toUri(), file2.toUri()),
                it.requireContext()
            )
        }

        scheduler.flush()

        onView(withId(org.odk.collect.maps.R.id.layers)).check(
            matches(
                RecyclerViewMatcher.withListSize(
                    2
                )
            )
        )
        onView(
            withRecyclerView(org.odk.collect.maps.R.id.layers).atPositionOnView(
                0,
                org.odk.collect.maps.R.id.layer_name
            )
        ).check(matches(withText(file2.name)))
        onView(
            withRecyclerView(org.odk.collect.maps.R.id.layers).atPositionOnView(
                1,
                org.odk.collect.maps.R.id.layer_name
            )
        ).check(matches(withText(file1.name)))
    }

    @Test
    fun `recreating maintains the list of selected layers`() {
        val file1 = TempFiles.createTempFile("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFile("layer2", MbtilesFile.FILE_EXTENSION)

        val scenario = launchFragment().onFragment {
            it.viewModel.loadLayersToImport(
                listOf(file1.toUri(), file2.toUri()),
                it.requireContext()
            )
        }

        scheduler.flush()

        scenario.recreate()

        onView(withId(org.odk.collect.maps.R.id.layers)).check(
            matches(
                RecyclerViewMatcher.withListSize(
                    2
                )
            )
        )
        onView(
            withRecyclerView(org.odk.collect.maps.R.id.layers).atPositionOnView(
                0,
                org.odk.collect.maps.R.id.layer_name
            )
        ).check(matches(withText(file1.name)))
        onView(
            withRecyclerView(org.odk.collect.maps.R.id.layers).atPositionOnView(
                1,
                org.odk.collect.maps.R.id.layer_name
            )
        ).check(matches(withText(file2.name)))
    }

    @Test
    fun `only mbtiles files are taken into account`() {
        val file1 = TempFiles.createTempFile("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFile("layer2", ".txt")

        launchFragment().onFragment {
            it.viewModel.loadLayersToImport(
                listOf(file1.toUri(), file2.toUri()),
                it.requireContext()
            )
        }

        scheduler.flush()

        onView(withId(org.odk.collect.maps.R.id.layers)).check(
            matches(
                RecyclerViewMatcher.withListSize(
                    1
                )
            )
        )
        onView(
            withRecyclerView(org.odk.collect.maps.R.id.layers).atPositionOnView(
                0,
                org.odk.collect.maps.R.id.layer_name
            )
        ).check(matches(withText(file1.name)))
    }

    @Test
    fun `clicking the 'add layer' button moves the files to the shared layers dir if it is selected`() {
        val file1 = TempFiles.createTempFile("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFile("layer2", MbtilesFile.FILE_EXTENSION)

        launchFragment().onFragment {
            it.viewModel.loadLayersToImport(
                listOf(file1.toUri(), file2.toUri()),
                it.requireContext()
            )
        }

        scheduler.flush()

        Interactions.clickOn(withId(org.odk.collect.maps.R.id.add_layer_button))
        scheduler.flush()

        assertThat(referenceLayerRepository.projectLayers.size, equalTo(0))
        assertThat(referenceLayerRepository.sharedLayers.size, equalTo(2))
        val filesInSharedLayersDir = referenceLayerRepository.sharedLayers.map { it.file.name }
        assertThat(filesInSharedLayersDir, containsInAnyOrder(file1.name, file2.name))
    }

    @Test
    fun `clicking the 'add layer' button moves the files to the project layers dir if it is selected`() {
        val file1 = TempFiles.createTempFile("layer1", MbtilesFile.FILE_EXTENSION)
        val file2 = TempFiles.createTempFile("layer2", MbtilesFile.FILE_EXTENSION)

        launchFragment().onFragment {
            it.viewModel.loadLayersToImport(
                listOf(file1.toUri(), file2.toUri()),
                it.requireContext()
            )
        }

        scheduler.flush()

        Interactions.clickOn(withId(org.odk.collect.maps.R.id.current_project_option))
        Interactions.clickOn(withId(org.odk.collect.maps.R.id.add_layer_button))
        scheduler.flush()

        assertThat(referenceLayerRepository.sharedLayers.size, equalTo(0))
        assertThat(referenceLayerRepository.projectLayers.size, equalTo(2))
        val filesInProjectLayersDir = referenceLayerRepository.projectLayers.map { it.file.name }
        assertThat(filesInProjectLayersDir, containsInAnyOrder(file1.name, file2.name))
    }

    @Test
    fun `the warning dialog is displayed if some selected files are not supported and the importer dialog is kept displayed`() {
        val file1 = TempFiles.createTempFile("layerA", ".txt")
        val file2 = TempFiles.createTempFile("layerB", MbtilesFile.FILE_EXTENSION)

        launchFragment().onFragment {
            it.viewModel.loadLayersToImport(
                listOf(file1.toUri(), file2.toUri()),
                it.requireContext()
            )

            scheduler.flush()

            val context = ApplicationProvider.getApplicationContext<Application>()
            onView(
                withText(
                    context.getLocalizedQuantityString(
                        R.plurals.non_mbtiles_files_selected_title,
                        1,
                        1
                    )
                )
            ).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withText(R.string.some_non_mbtiles_files_selected_message)).inRoot(isDialog())
                .check(matches(isDisplayed()))
            onView(withText(R.string.ok)).inRoot(isDialog()).check(matches(isDisplayed()))

            assertThat(it.isVisible, equalTo(true))
        }
    }

    @Test
    fun `the warning dialog is displayed if all selected files are not supported and the importer dialog is dismissed`() {
        val file = TempFiles.createTempFile("layerA", ".txt")

        launchFragment().onFragment {
            it.viewModel.loadLayersToImport(listOf(file.toUri()), it.requireContext())

            scheduler.flush()

            val context = ApplicationProvider.getApplicationContext<Application>()
            onView(
                withText(
                    context.getLocalizedQuantityString(
                        R.plurals.non_mbtiles_files_selected_title,
                        1,
                        1
                    )
                )
            ).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withText(R.string.all_non_mbtiles_files_selected_message)).inRoot(isDialog())
                .check(matches(isDisplayed()))
            onView(withText(R.string.ok)).inRoot(isDialog()).check(matches(isDisplayed()))

            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `the warning dialog shows correct number of unsupported layers`() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        launchFragment().onFragment {
            // Three unsupported layers
            it.viewModel.loadLayersToImport(
                listOf(
                    TempFiles.createTempFile("layerA", ".txt").toUri(),
                    TempFiles.createTempFile("layerB", ".txt").toUri(),
                    TempFiles.createTempFile("layerC", ".txt").toUri()
                ),
                context
            )
            scheduler.flush()
            onView(
                withText(
                    context.getLocalizedQuantityString(
                        R.plurals.non_mbtiles_files_selected_title,
                        3,
                        3
                    )
                )
            ).inRoot(isDialog()).check(matches(isDisplayed()))
        }

        launchFragment().onFragment {
            // Two unsupported layers
            it.viewModel.loadLayersToImport(
                listOf(
                    TempFiles.createTempFile("layerA", ".txt").toUri(),
                    TempFiles.createTempFile("layerB", ".txt").toUri(),
                    TempFiles.createTempFile("layerC", MbtilesFile.FILE_EXTENSION).toUri()
                ),
                context
            )
            scheduler.flush()
            onView(
                withText(
                    context.getLocalizedQuantityString(
                        R.plurals.non_mbtiles_files_selected_title,
                        2,
                        2
                    )
                )
            ).inRoot(isDialog()).check(matches(isDisplayed()))
        }

        launchFragment().onFragment {
            // One unsupported layer
            it.viewModel.loadLayersToImport(
                listOf(
                    TempFiles.createTempFile("layerA", ".txt").toUri(),
                    TempFiles.createTempFile("layerB", MbtilesFile.FILE_EXTENSION).toUri(),
                    TempFiles.createTempFile("layerC", MbtilesFile.FILE_EXTENSION).toUri()
                ),
                context
            )
            scheduler.flush()
            onView(
                withText(
                    context.getLocalizedQuantityString(
                        R.plurals.non_mbtiles_files_selected_title,
                        1,
                        1
                    )
                )
            ).inRoot(isDialog()).check(matches(isDisplayed()))
        }
    }

    private fun launchFragment(): FragmentScenario<OfflineMapLayersImporterDialogFragment> {
        return fragmentScenarioLauncherRule.launchInContainer(OfflineMapLayersImporterDialogFragment::class.java)
    }
}
