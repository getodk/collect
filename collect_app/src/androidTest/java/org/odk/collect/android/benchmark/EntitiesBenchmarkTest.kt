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
import org.odk.collect.strings.R

private const val PROJECT_URL = ""

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
        assertThat("Need to set PROJECT_URL before running!", PROJECT_URL, not(blankOrNullString()))
        clearAndroidCache()

        val stopwatch = Stopwatch()

        rule.startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(PROJECT_URL)
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
            .benchmark("Downloading form with http cache", stopwatch) {
                it.clickGetSelected()
            }

            .clickOK(MainMenuPage())
            .clickGetBlankForm()
            .benchmark("Updating form with http cache", stopwatch) {
                it.clickGetSelected()
            }

            .clickOK(MainMenuPage())
            .clickFillBlankForm()
            .benchmark("Loading form first time", stopwatch) {
                it.clickOnForm("100k Entities Filter")
            }

            .pressBackAndDiscardForm()
            .clickFillBlankForm()
            .benchmark("Loading form second time", stopwatch) {
                it.clickOnForm("100k Entities Filter")
            }

            .answerQuestion("Which value do you want to filter by?", "1024")
            .benchmark("Filtering select", stopwatch) {
                it.swipeToNextQuestion("Filtered select")
            }

        assertThat(stopwatch.getTime("Downloading form with http cache"), lessThan(75))
        assertThat(stopwatch.getTime("Updating form with http cache"), lessThan(90))
        assertThat(stopwatch.getTime("Loading form first time"), lessThan(5))
        assertThat(stopwatch.getTime("Loading form second time"), lessThan(5))
        assertThat(stopwatch.getTime("Filtering select"), lessThan(5))
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

        times[name] = (endTime - startTime) / 1000
        return result
    }

    fun getTime(name: String): Long {
        return times[name]!!
    }
}

private fun <T : Page<T>, Y : Page<Y>> Y.benchmark(
    name: String,
    stopwatch: Stopwatch,
    action: (Y) -> T
): T {
    return stopwatch.time(name) {
        action(this)
    }
}
