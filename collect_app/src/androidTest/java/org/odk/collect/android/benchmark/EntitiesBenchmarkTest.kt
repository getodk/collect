package org.odk.collect.android.benchmark

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.blankOrNullString
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.benchmark.support.Benchmarker
import org.odk.collect.android.benchmark.support.benchmark
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.android.test.BuildConfig.ENTITIES_FILTER_PROJECT_URL
import org.odk.collect.android.test.BuildConfig.THOUSAND_MEDIA_FILE_ENTITY_LIST_PROJECT_URL

@RunWith(AndroidJUnit4::class)
class EntitiesBenchmarkTest {

    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    var chain: RuleChain = chain(TestDependencies(true)).around(rule)

    /**
     * Benchmarks the performance of entity follow up forms. [ENTITIES_FILTER_PROJECT_URL] should
     * be set to a project that contains the "100k Entities Filter" benchmark form and the
     * "entities_100k" entity list.
     *
     * Devices that currently pass:
     * - Fairphone 3
     * - Pixel 3
     *
     */
    @Test
    fun oneHundredThousandEntities() {
        assertThat(
            "Need to set ENTITIES_FILTER_PROJECT_URL before running!",
            ENTITIES_FILTER_PROJECT_URL,
            not(blankOrNullString())
        )
        clearAndroidCache()

        val benchmarker = Benchmarker()

        rule.startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(ENTITIES_FILTER_PROJECT_URL)
            .addProject()

            // Populate http cache and recreate project
            .clickGetBlankForm()
            .clickGetSelected()
            .clickOK(MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .deleteLastProject()
            .clickManuallyEnterProjectDetails()
            .inputUrl(ENTITIES_FILTER_PROJECT_URL)
            .addProject()

            .clickGetBlankForm()
            .benchmark("Downloading form with http cache", 40, benchmarker) {
                it.clickGetSelected()
            }

            .clickOK(MainMenuPage())
            .clickGetBlankForm()
            .benchmark("Downloading form second time with http cache", 5, benchmarker) {
                it.clickGetSelected()
            }

            .clickOK(MainMenuPage())
            .clickFillBlankForm()
            .benchmark("Loading form first time", 2, benchmarker) {
                it.clickOnForm("100k Entities Filter")
            }

            .pressBackAndDiscardForm()
            .clickFillBlankForm()
            .benchmark("Loading form second time", 2, benchmarker) {
                it.clickOnForm("100k Entities Filter")
            }

            .answerQuestion("Which value do you want to filter by?", "1024")
            .benchmark("Filtering select", 3, benchmarker) {
                it.swipeToNextQuestion("Filtered select")
            }

        benchmarker.assertResults()
    }

    /**
     * Benchmarks the performance of updating forms with entity lists and many media files.
     * [THOUSAND_MEDIA_FILE_ENTITY_LIST_PROJECT_URL] should be set to a project that contains the
     * "1000-media-files-entity-list" form.
     *
     * Devices that currently pass:
     * - Fairphone 3
     */
    @Test
    fun oneThousandMediaFilesWithEntityList() {
        assertThat(
            "Need to set THOUSAND_MEDIA_FILE_ENTITY_LIST_PROJECT_URL before running!",
            THOUSAND_MEDIA_FILE_ENTITY_LIST_PROJECT_URL,
            not(blankOrNullString())
        )

        val benchmarker = Benchmarker()

        rule.startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(THOUSAND_MEDIA_FILE_ENTITY_LIST_PROJECT_URL)
            .addProject()

            // Download all forms
            .clickGetBlankForm()
            .clickGetSelected()
            .clickOKOnDialog(MainMenuPage())

            .clickGetBlankForm()
            .benchmark(
                "Redownloading a form with 1k media files and entity list when there are no updates",
                15,
                benchmarker
            ) {
                it
                    .clickGetSelected()
                    .clickOKOnDialog(MainMenuPage())
            }

        benchmarker.assertResults()
    }
}

private fun clearAndroidCache() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    application.cacheDir.deleteRecursively()
    application.cacheDir.mkdir()
}
