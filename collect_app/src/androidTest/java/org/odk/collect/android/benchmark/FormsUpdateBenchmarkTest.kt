package org.odk.collect.android.benchmark

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.blankOrNullString
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.application.FeatureFlags
import org.odk.collect.android.benchmark.support.Benchmarker
import org.odk.collect.android.benchmark.support.benchmark
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.android.test.BuildConfig.THOUSAND_MEDIA_FILE_ENTITY_LIST_PROJECT_URL
import org.odk.collect.android.test.BuildConfig.THOUSAND_MEDIA_FILE_PROJECT_URL

@RunWith(AndroidJUnit4::class)
class FormsUpdateBenchmarkTest {
    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    val chain: RuleChain = chain(TestDependencies(true)).around(rule)

    /**
     * Benchmarks the performance of updating forms. [THOUSAND_MEDIA_FILE_PROJECT_URL] should
     * be set to a project that contains the "1000-media-files" benchmark form.
     *
     * Devices that currently pass:
     * - Fairphone 3
     */
    @Test
    fun oneThousandMediaFiles() {
        assertThat(
            "Need to set THOUSAND_MEDIA_FILE_PROJECT_URL before running!",
            THOUSAND_MEDIA_FILE_PROJECT_URL,
            not(blankOrNullString())
        )

        val benchmarker = Benchmarker()

        rule.startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(THOUSAND_MEDIA_FILE_PROJECT_URL)
            .addProject()

            // Download all forms
            .clickGetBlankForm()
            .clickGetSelected()
            .clickOKOnDialog(MainMenuPage())

            .benchmark(
                "Fetching form list with 1k media files when there are no updates",
                7,
                benchmarker
            ) {
                it
                    .clickGetBlankForm()
                    .clickSelectAll()
            }

            .benchmark(
                "Redownloading a form with 1k media files when there are no updates",
                12,
                benchmarker
            ) {
                it
                    .clickGetSelected()
                    .clickOKOnDialog(MainMenuPage())
            }

        benchmarker.assertResults()
    }

    /**
     * Benchmarks the performance of updating forms. [THOUSAND_MEDIA_FILE_ENTITY_LIST_PROJECT_URL] should
     * be set to a project that contains the "1000-media-files-entity-list" form.
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
                if (FeatureFlags.FASTER_FORM_UPDATES) 5 else 15,
                benchmarker
            ) {
                it
                    .clickGetSelected()
                    .clickOKOnDialog(MainMenuPage())
            }

        benchmarker.assertResults()
    }
}
