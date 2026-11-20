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
        testDependencies.server.addForm("one-question.xml")
        testDependencies.server.addForm("one-question-repeat.xml")

        val page = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .also { testDependencies.scheduler.runDeferredTasks() }
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
        testDependencies.server.removeForm("One Question Repeat")

        page.clickRefresh()
            .assertText("Two Question") // Check new form downloaded
            .assertText("One Question Updated") // Check updated form updated
            .assertTextDoesNotExist("One Question Repeat") // Check deleted form deleted
    }

    @Test
    fun whenMatchExactlyEnabled_clickingFillBlankForm_andClickingRefresh_whenThereIsAnErrorFetchingList_showsNotification_andClickingNotification_returnsToApp() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .also {
                testDependencies.scheduler.runDeferredTasks()
                testDependencies.server.alwaysReturnError()
            }
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
    fun whenMatchExactlyEnabled_clickingFillBlankForm_andClickingRefresh_whenThereIsAnErrorDownloadingAForm_showsNotification_andClickingNotification_returnsToApp() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .also {
                testDependencies.scheduler.runDeferredTasks()

                testDependencies.server.addForm("one-question.xml")
                testDependencies.server.errorOnFetchingForms()
            }
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
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .also {
                testDependencies.scheduler.runDeferredTasks()
                testDependencies.server.alwaysReturnError()
            }
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
        testDependencies.server.addForm("one-question.xml")

        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .also {
                testDependencies.scheduler.runDeferredTasks()
                testDependencies.server.setCredentials("Klay", "Thompson")
            }
            .clickFillBlankForm()
            .clickRefreshWithAuthError()
            .fillUsername("Klay")
            .fillPassword("Thompson")
            .clickOK(FillBlankFormPage())
            .clickRefresh()
            .assertText("One Question")
    }

    @Test
    fun whenMatchExactlyEnabled_getsLatestFormsFromServer_automaticallyAndRepeatedly() {
        testDependencies.server.addForm("one-question.xml")
        testDependencies.server.addForm("one-question-repeat.xml")

        // Initial download
        var page = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .also {
                testDependencies.scheduler.runDeferredTasks()
            }.clickFillBlankForm()
            .assertText("One Question")
            .assertText("One Question Repeat")

        testDependencies.server.addForm("Two Question", "two_question", "1", "two-question.xml")
        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )
        testDependencies.server.removeForm("One Question Repeat")

        testDependencies.scheduler.runDeferredTasks()
        page = page
            .assertText("Two Question") // Check new form downloaded
            .assertText("One Question Updated") // Check updated form updated
            .assertTextDoesNotExist("One Question Repeat") // Check deleted form deleted

        testDependencies.scheduler.runDeferredTasks()
        page.assertText("One Question Updated")
            .assertText("Two Question")
            .assertTextDoesNotExist("One Question Repeat")
    }

    @Test
    fun whenMatchExactlyEnabled_andThereIsAnErrorFetchingList_showsNotification_andClickingNotification_returnsToApp() {
        testDependencies.server.alwaysReturnError()

        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()

        testDependencies.scheduler.runDeferredTasks()

        notificationDrawerRule
            .open()
            .assertNotification("ODK Collect", "Form update failed", "Demo project")
            .clickNotification(
                "ODK Collect",
                "Form update failed",
                MainMenuPage()
            )
    }

    @Test
    fun whenMatchExactlyEnabled_andThereIsAnErrorDownloadingAForm_showsNotification_andClickingNotification_returnsToApp() {
        testDependencies.server.errorOnFetchingForms()
        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )

        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()

        testDependencies.scheduler.runDeferredTasks()

        notificationDrawerRule
            .open()
            .assertNotification("ODK Collect", "Form update failed", "Demo project")
            .clickNotification(
                "ODK Collect",
                "Form update failed",
                MainMenuPage()
            )
    }

    @Test
    fun whenMatchExactlyEnabled_hidesGetBlankFormsAndDeleteBlankForms() {
        rule.startAtMainMenu()
            .enableMatchExactly()
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.get_forms)
            .clickDeleteSavedForm()
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.forms)
    }

    @Test
    fun whenMatchExactlyDisabled_stopsSyncingAutomatically() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .enableManualUpdates()

        assertThat(testDependencies.scheduler.getDeferredTasks(), `is`(empty()))
    }

    @Test
    fun whenMatchExactlyEnabled_getsLatestFormsFromServerDuringFillingAFormWithoutEntities() {
        testDependencies.server.addForm("one-question.xml")

        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .also { testDependencies.scheduler.runDeferredTasks() }
            .startBlankForm("One Question")
            .also {
                testDependencies.server.addForm(
                    "One Question Updated",
                    "one_question",
                    "2",
                    "one-question-updated.xml"
                )

                testDependencies.scheduler.runDeferredTasks()
            }
            .pressBackAndDiscardForm()
            .clickFillBlankForm()
            .assertFormExists("One Question Updated")
    }

    @Test
    fun whenMatchExactlyEnabled_doesNotGetLatestFormsFromServerDuringFillingAFormWithEntities() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm("one-question.xml")

        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .also { testDependencies.scheduler.runDeferredTasks() }
            .startBlankForm("One Question Entity Registration")
            .also {
                testDependencies.server.addForm(
                    "One Question Updated",
                    "one_question",
                    "2",
                    "one-question-updated.xml"
                )

                testDependencies.scheduler.runDeferredTasks()
            }
            .pressBackAndDiscardForm()
            .clickFillBlankForm()
            .assertFormDoesNotExist("One Question Updated")
    }

    @Test
    fun whenMatchExactlyStopped_andFails_showNotification_andOpenFormListOnClick() {
        testDependencies.server.alwaysReturnError()

        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()

        testDependencies.scheduler.runDeferredTasks(isLastUniqueExecution = false, isStopped = true)

        notificationDrawerRule
            .open()
            .assertNotification("ODK Collect", "Form update failed", "Demo project")
            .clickNotification(
                "ODK Collect",
                "Form update failed",
                FillBlankFormPage()
            )
    }

    @Test
    fun whenMatchExactlyStopped_andSucceeds_showNotification_andOpenFormListOnClick() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()

        testDependencies.scheduler.runDeferredTasks(isLastUniqueExecution = false, isStopped = true)

        notificationDrawerRule
            .open()
            .assertNoNotification("ODK Collect")
    }
}
