package org.odk.collect.android.benchmark

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.blankOrNullString
import org.hamcrest.Matchers.lessThan
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.Page
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.android.test.BuildConfig.ENTITIES_FILTER_TEST_PROJECT_URL
import org.odk.collect.shared.TimeInMs
import org.odk.collect.strings.R

/**
 * Benchmarks the performance of entity follow up forms. [PROJECT_URL] should be set to a project
 * that contains the "100k Entities Filter" form.
 *
 * Devices that currently pass:
 * - Pixel 4a
 * - Fairphone 3
 *
 */

@RunWith(AndroidJUnit4::class)
class EntitiesBenchmarkTest {

    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    var chain: RuleChain = chain(TestDependencies(true)).around(rule)

    @Test
    fun run() {
        assertThat(
            "Need to set ENTITIES_FILTER_TEST_PROJECT_URL before running!",
            ENTITIES_FILTER_TEST_PROJECT_URL,
            not(blankOrNullString())
        )
        clearAndroidCache()

        val benchmarker = Benchmarker()

        rule.startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(ENTITIES_FILTER_TEST_PROJECT_URL)
            .addProject()

            // Populate http cache and clear out form/entities
            .clickGetBlankForm()
            .clickGetSelected()
            .clickOK(MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetProject()
            .clickOnString(R.string.reset_blank_forms)
            .clickOnString(R.string.reset_saved_forms)
            .clickOnString(R.string.reset_settings_button_reset)
            .clickOKOnDialog(MainMenuPage())

            .clickGetBlankForm()
            .benchmark("Downloading form with http cache", 75, benchmarker) {
                it.clickGetSelected()
            }

            .clickOK(MainMenuPage())
            .clickGetBlankForm()
            .benchmark("Updating form with http cache", 90, benchmarker) {
                it.clickGetSelected()
            }

            .clickOK(MainMenuPage())
            .clickFillBlankForm()
            .benchmark("Loading form first time", 5, benchmarker) {
                it.clickOnForm("100k Entities Filter")
            }

            .pressBackAndDiscardForm()
            .clickFillBlankForm()
            .benchmark("Loading form second time", 5, benchmarker) {
                it.clickOnForm("100k Entities Filter")
            }

            .answerQuestion("Which value do you want to filter by?", "1024")
            .benchmark("Filtering select", 5, benchmarker) {
                it.swipeToNextQuestion("Filtered select")
            }

        benchmarker.assertResults()
    }

    private fun clearAndroidCache() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        application.cacheDir.deleteRecursively()
        application.cacheDir.mkdir()
    }
}

private class Stopwatch {

    private val times = mutableMapOf<String, Long>()

    fun <T> time(name: String, action: () -> T): T {
        val startTime = System.currentTimeMillis()
        val result = action()
        val endTime = System.currentTimeMillis()

        times[name] = (endTime - startTime) / TimeInMs.ONE_SECOND
        return result
    }

    fun getTime(name: String): Long {
        return times[name]!!
    }
}

private fun <T : Page<T>, Y : Page<Y>> Y.benchmark(
    name: String,
    target: Long,
    benchmarker: Benchmarker,
    action: (Y) -> T
): T {
    return benchmarker.benchmark(name, target) {
        action(this)
    }
}

private class Benchmarker {
    private val stopwatch = Stopwatch()
    private val targets = mutableMapOf<String, Long>()

    fun <T> benchmark(name: String, target: Long, action: () -> T): T {
        targets[name] = target
        return stopwatch.time(name) {
            action()
        }
    }

    fun assertResults() {
        printResults()

        targets.entries.forEach {
            val time = stopwatch.getTime(it.key)
            assertThat("\"${it.key}\" took ${time}s!", time, lessThan(it.value))
        }
    }

    private fun printResults() {
        println("Benchmark results:")
        targets.keys.forEach {
            println("$it: ${stopwatch.getTime(it)}s")
        }
    }
}
