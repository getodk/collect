package org.odk.collect.android.feature.formmanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.ErrorPage
import org.odk.collect.android.support.pages.FillBlankFormPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.NotificationDrawerRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class MatchExactlyTest {
    private val rule = CollectTestRule()
    private val testDependencies = TestDependencies()
    private val notificationDrawerRule = NotificationDrawerRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(notificationDrawerRule)
        .around(rule)

    @Test
    fun whenMatchExactlyEnabled_clickingFillBlankForm_andClickingRefresh_getsLatestFormsFromServer() {
        val page = rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .copyForm("one-question-repeat.xml")
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .clickFillBlankForm()
            .assertText("One Question")
            .assertText("One Question Repeat")

        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )

        testDependencies.server.addForm("Two Question", "two_question", "1", "two-question.xml")

        page.clickRefresh()
            .assertText("Two Question") // Check new form downloaded
            .assertText("One Question Updated") // Check updated form updated
            .assertTextDoesNotExist("One Question Repeat") // Check deleted form deleted
    }

    @Test
    fun whenMatchExactlyEnabled_clickingFillBlankForm_andClickingRefresh_whenThereIsAnError_showsNotification_andClickingNotification_returnsToApp() {
        testDependencies.server.alwaysReturnError()

        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .copyForm("one-question-repeat.xml")
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .clickFillBlankForm()
            .clickRefreshWithError()

        notificationDrawerRule
            .open()
            .assertNotification("ODK Collect", "Form update failed", "Demo project")
            .clickNotification(
                "ODK Collect",
                "Form update failed",
                FillBlankFormPage()
            ).pressBack(MainMenuPage())
    }

    @Test
    fun whenMatchExactlyEnabled_clickingFillBlankForm_andClickingRefresh_whenThereIsAnError_showsNotification_andClickingShowDetails_showsErrorDetails() {
        testDependencies.server.alwaysReturnError()

        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .copyForm("one-question-repeat.xml")
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .clickFillBlankForm()
            .clickRefreshWithError()

        notificationDrawerRule
            .open()
            .assertNotification("ODK Collect", "Form update failed", "Demo project")
            .clickAction(
                "ODK Collect",
                "Form update failed",
                "Show details",
                ErrorPage()
            ).assertText(org.odk.collect.strings.R.string.form_update_error)
            .assertText("Demo project")
            .assertText("The server https://server.example.com returned status code 500. If you keep having this problem, report it to the person who asked you to collect data.")
    }

    @Test
    fun whenMatchExactlyEnabled_clickingFillBlankForm_andClickingRefresh_whenThereIsAnAuthenticationError_promptsForCredentials() {
        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )

        testDependencies.server.setCredentials("Klay", "Thompson")

        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .copyForm("one-question-repeat.xml")
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .clickFillBlankForm()
            .clickRefreshWithAuthError()
            .fillUsername("Klay")
            .fillPassword("Thompson")
            .clickOK(FillBlankFormPage())
            .clickRefresh()
            .assertText("One Question Updated")
    }

    @Test
    fun whenMatchExactlyEnabled_getsLatestFormsFromServer_automaticallyAndRepeatedly() {
        var page = rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .copyForm("one-question-repeat.xml")
            .setServer(testDependencies.server.url)
            .enableMatchExactly()

        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )

        testDependencies.server.addForm("Two Question", "two_question", "1", "two-question.xml")

        testDependencies.scheduler.runDeferredTasks()

        page = page.clickFillBlankForm()
            .assertText("Two Question")
            .assertText("One Question Updated")
            .assertTextDoesNotExist("One Question Repeat")
            .pressBack(MainMenuPage())

        testDependencies.server.removeForm("Two Question")

        testDependencies.scheduler.runDeferredTasks()

        page.assertOnPage()
            .clickFillBlankForm()
            .assertText("One Question Updated")
            .assertTextDoesNotExist("Two Question")
    }

    @Test
    fun whenMatchExactlyEnabled_hidesGetBlankFormsAndDeleteBlankForms() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .copyForm("one-question-repeat.xml")
            .enableMatchExactly()
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.get_forms)
            .clickDeleteSavedForm()
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.forms)
    }

    @Test
    fun whenMatchExactlyDisabled_stopsSyncingAutomatically() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .copyForm("one-question-repeat.xml")
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .enableManualUpdates()

        assertThat(testDependencies.scheduler.getDeferredTasks(), `is`(empty()))
    }
}
