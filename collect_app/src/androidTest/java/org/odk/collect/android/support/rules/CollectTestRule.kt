package org.odk.collect.android.support.rules

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.NoMatchingViewException
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.odk.collect.android.activities.SplashScreenActivity
import org.odk.collect.android.external.AndroidShortcutsActivity
import org.odk.collect.android.injection.DaggerUtils.getComponent
import org.odk.collect.android.support.pages.FirstLaunchPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.Page
import org.odk.collect.android.support.pages.ShortcutsPage
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import java.util.function.Consumer

class CollectTestRule @JvmOverloads constructor(
    private val useDemoProject: Boolean = true
) : ActivityScenarioLauncherRule() {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                launch(SplashScreenActivity::class.java)

                val firstLaunchPage = FirstLaunchPage()

                /**
                 * We've seen some failures here that look like they could be to do with our
                 * state not being reset properly. Here we catch a view assertion and throw
                 * a specific exception if that looks like it's the case to help debugging.
                 */
                try {
                    firstLaunchPage.assertOnPage()
                } catch (e: NoMatchingViewException) {
                    val application = ApplicationProvider.getApplicationContext<Application>()
                    val projectsRepository = getComponent(application).projectsRepository()

                    if (projectsRepository.getAll().isNotEmpty()) {
                        throw IllegalStateException("Projects not reset properly!")
                    } else {
                        throw e
                    }
                }

                if (useDemoProject) {
                    firstLaunchPage.clickTryCollect()
                }

                base.evaluate()
            }
        }
    }

    fun startAtMainMenu() = MainMenuPage()

    fun startAtFirstLaunch() = FirstLaunchPage()

    fun withProject(serverUrl: String): MainMenuPage =
        startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(serverUrl)
            .addProject()

    fun launchShortcuts(): ShortcutsPage {
        val scenario = launch(AndroidShortcutsActivity::class.java)
        return ShortcutsPage(scenario).assertOnPage()
    }

    fun <T : Page<T>> launch(intent: Intent, destination: T): T {
        launch<Activity>(intent)
        return destination.assertOnPage()
    }

    fun <T : Page<T>> launchForResult(
        intent: Intent,
        destination: T,
        actions: Consumer<T>
    ): Instrumentation.ActivityResult {
        val scenario = launch<Activity>(intent)
        destination.assertOnPage()
        actions.accept(destination)
        return scenario.result
    }
}
