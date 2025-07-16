package org.odk.collect.android.benchmark

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
import org.odk.collect.android.test.BuildConfig.ENTITIES_FILTER_SEARCH_PROJECT_URL

/**
 * Benchmarks the performance of search() forms. [ENTITIES_FILTER_SEARCH_PROJECT_URL] should be
 * set to a project that contains the "100k Entities Filter search()" benchmark form and the
 * "entities_100k" entity list.
 *
 * Devices that currently pass:
 * - Fairphone 3
 *
 */

@RunWith(AndroidJUnit4::class)
class SearchBenchmarkTest {

    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    var chain: RuleChain = chain(TestDependencies(true)).around(rule)

    @Test
    fun run() {
        assertThat(
            "Need to set ENTITIES_FILTER_SEARCH_PROJECT_URL before running!",
            ENTITIES_FILTER_SEARCH_PROJECT_URL,
            not(blankOrNullString())
        )

        val benchmarker = Benchmarker()

        rule.startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(ENTITIES_FILTER_SEARCH_PROJECT_URL)
            .addProject()

            .clickGetBlankForm()
            .clickGetSelected()
            .clickOK(MainMenuPage())

            .clickFillBlankForm()
            .benchmark("Loading form first time", 20, benchmarker) {
                it.clickOnForm("100k Entities Filter search()")
            }

            .pressBackAndDiscardForm()
            .clickFillBlankForm()
            .benchmark("Loading form second time", 2, benchmarker) {
                it.clickOnForm("100k Entities Filter search()")
            }

            .answerQuestion("Which value do you want to filter by?", "1024")
            .benchmark("Filtering select", 2, benchmarker) {
                it.swipeToNextQuestion("Filtered select")
            }

        benchmarker.assertResults()
    }
}
