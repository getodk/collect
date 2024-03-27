package org.odk.collect.android.support.rules

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import org.odk.collect.android.external.AndroidShortcutsActivity
import org.odk.collect.android.support.ActivityHelpers.getLaunchIntent
import org.odk.collect.android.support.StubOpenRosaServer
import org.odk.collect.android.support.pages.FirstLaunchPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.Page
import org.odk.collect.android.support.pages.ShortcutsPage
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import java.util.function.Consumer

class CollectTestRule @JvmOverloads constructor(
    private val useDemoProject: Boolean = true
) : ActivityScenarioLauncherRule() {

    override fun before() {
        super.before()

        val firstLaunchPage = launch(
            getLaunchIntent(),
            FirstLaunchPage()
        ).assertOnPage()

        if (useDemoProject) {
            firstLaunchPage.clickTryCollect()
        }
    }

    fun startAtMainMenu() = MainMenuPage()

    fun startAtFirstLaunch() = FirstLaunchPage()

    fun withProject(serverUrl: String): MainMenuPage {
        return startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(serverUrl)
            .addProject()
    }

    fun withMatchExactlyProject(serverUrl: String): MainMenuPage {
        return startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(serverUrl)
            .addProject()
            .enableMatchExactly()
            .clickFillBlankForm()
            .clickRefresh()
            .pressBack(MainMenuPage())
    }

    fun withProject(testServer: StubOpenRosaServer, vararg formFiles: String): MainMenuPage {
        val mainMenuPage = startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(testServer.url)
            .addProject()

        return if (formFiles.isNotEmpty()) {
            formFiles.fold(mainMenuPage) { page, formFile -> page.copyForm(formFile, testServer.hostName) }
        } else {
            mainMenuPage
        }
    }

    fun launchShortcuts(): ShortcutsPage {
        val scenario = launchForResult(AndroidShortcutsActivity::class.java)
        return ShortcutsPage(scenario).assertOnPage()
    }

    fun <D : Page<D>> relaunch(destination: D): D {
        return launch(getLaunchIntent(), destination)
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
        val scenario = launchForResult<Activity>(intent)
        destination.assertOnPage()
        actions.accept(destination)
        return scenario.result
    }
}
