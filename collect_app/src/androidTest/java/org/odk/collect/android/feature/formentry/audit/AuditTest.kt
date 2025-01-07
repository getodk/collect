package org.odk.collect.android.feature.formentry.audit

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.StorageUtils
import org.odk.collect.android.support.pages.AccessControlPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.RecentAppsRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class AuditTest {

    private val rule = CollectTestRule()
    private val recentAppsRule = RecentAppsRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain()
        .around(recentAppsRule)
        .around(rule)

    @Test
    fun fillAndEditingForm_updatesAuditLogForInstance() {
        rule.startAtMainMenu()
            .copyForm("one-question-audit.xml")
            .startBlankForm("One Question Audit")
            .fillOut(
                FormEntryPage.QuestionAndAnswer("what is your age", "31")
            )
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .clickDrafts(1)
            .clickOnForm("One Question Audit")
            .clickGoToStart()
            .fillOutAndFinalize(
                FormEntryPage.QuestionAndAnswer("what is your age", "32")
            )

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(12))

        assertThat(auditLog[0].get("event"), equalTo("form start"))
        assertThat(auditLog[1].get("event"), equalTo("question"))
        assertThat(auditLog[2].get("event"), equalTo("end screen"))
        assertThat(auditLog[3].get("event"), equalTo("form save"))
        assertThat(auditLog[4].get("event"), equalTo("form exit"))

        assertThat(auditLog[5].get("event"), equalTo("form resume"))
        assertThat(auditLog[6].get("event"), equalTo("jump"))
        assertThat(auditLog[7].get("event"), equalTo("question"))
        assertThat(auditLog[8].get("event"), equalTo("end screen"))
        assertThat(auditLog[9].get("event"), equalTo("form save"))
        assertThat(auditLog[10].get("event"), equalTo("form exit"))
        assertThat(auditLog[11].get("event"), equalTo("form finalize"))
    }

    @Test // https://github.com/getodk/collect/issues/5551
    fun navigatingToSettings_savesAnswersFromCurrentScreenToAuditLog() {
        rule.startAtMainMenu()
            .copyForm("two-question-audit-track-changes.xml")
            .startBlankForm("One Question Audit Track Changes")
            .fillOut(FormEntryPage.QuestionAndAnswer("What is your age?", "31"))
            .clickOptionsIcon()
            .clickProjectSettings()

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog[1].get("event"), equalTo("question"))
        assertThat(auditLog[1].get("new-value"), equalTo("31"))
    }

    @Test // https://github.com/getodk/collect/issues/5900
    fun navigatingToNextQuestion_savesAnswersFromCurrentScreenToAuditLog() {
        rule.startAtMainMenu()
            .copyForm("two-question-audit-track-changes.xml")
            .startBlankForm("One Question Audit Track Changes")
            .fillOut(FormEntryPage.QuestionAndAnswer("What is your age?", "31"))
            .swipeToNextQuestion("What is your name?")
            .fillOut(FormEntryPage.QuestionAndAnswer("What is your name?", "Adam"))
            .swipeToEndScreen()

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog[1].get("event"), equalTo("question"))
        assertThat(auditLog[1].get("new-value"), equalTo("31"))
        assertThat(auditLog[2].get("new-value"), equalTo("Adam"))
    }

    @Test // https://github.com/getodk/collect/issues/5253
    fun navigatingBackToTheFormAfterKillingTheAppWhenMovingBackwardsIsDisabled_savesFormResumeEventToAuditLog() {
        rule.startAtMainMenu()
            .copyForm("one-question-audit.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickMovingBackwards()
            .clickOnString(org.odk.collect.strings.R.string.yes)
            .pressBack(AccessControlPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .startBlankForm("One Question Audit")
            .fillOut(FormEntryPage.QuestionAndAnswer("what is your age", "31"))
            .killAndReopenApp(rule, recentAppsRule, MainMenuPage())
            .startBlankFormWithSavepoint("One Question Audit")
            .clickRecover(FormEntryPage("One Question Audit"))
            .swipeToEndScreen()
            .clickFinalize()

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog[1].get("event"), equalTo("form resume"))
    }

    @Test // https://github.com/getodk/collect/issues/5659
    fun savingFormWithBackgroundRecording_doesNotDuplicateAnyEvent() {
        rule.startAtMainMenu()
            .copyForm("one-question-background-audio-audit.xml")
            .startBlankForm("One Question Background Audio And Audit")
            .fillOutAndSave(
                MainMenuPage(),
                FormEntryPage.QuestionAndAnswer("what is your age", "31")
            )

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(4))

        assertThat(auditLog[0].get("event"), equalTo("form start"))
        assertThat(auditLog[1].get("event"), equalTo("question"))
        assertThat(auditLog[2].get("event"), equalTo("form save"))
        assertThat(auditLog[3].get("event"), equalTo("form exit"))
    }

    @Test // https://github.com/getodk/collect/issues/5262
    fun locationTrackingEventsShouldBeLoggedBeforeQuestionEventsWhenANewFormIsStartedOrAPreviouslySavedOneEdited() {
        rule.startAtMainMenu()
            .copyForm("location-audit.xml")
            .startBlankForm("Audit with Location")
            .pressBackAndSaveAsDraft()
            .clickDrafts()
            .clickOnForm("Audit with Location")
            .clickGoToStart()
            .pressBackAndSaveAsDraft()

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(15))

        assertThat(auditLog[0].get("event"), equalTo("form start"))
        assertThat(auditLog[1].get("event"), equalTo("location tracking enabled"))
        assertThat(auditLog[2].get("event"), equalTo("location permissions granted"))
        assertThat(auditLog[3].get("event"), equalTo("location providers enabled"))
        assertThat(auditLog[4].get("event"), equalTo("question"))
        assertThat(auditLog[5].get("event"), equalTo("form save"))
        assertThat(auditLog[6].get("event"), equalTo("form exit"))
        assertThat(auditLog[7].get("event"), equalTo("form resume"))
        assertThat(auditLog[8].get("event"), equalTo("jump"))
        assertThat(auditLog[9].get("event"), equalTo("location tracking enabled"))
        assertThat(auditLog[10].get("event"), equalTo("location permissions granted"))
        assertThat(auditLog[11].get("event"), equalTo("location providers enabled"))
        assertThat(auditLog[12].get("event"), equalTo("question"))
        assertThat(auditLog[13].get("event"), equalTo("form save"))
        assertThat(auditLog[14].get("event"), equalTo("form exit"))
    }

    @Test // https://github.com/getodk/collect/issues/5915
    fun changingTheAnswerAfterSavingAFormOnTheSamePage_shouldLogTheNewAnswer() {
        rule.startAtMainMenu()
            .copyForm("two-question-audit-track-changes.xml")
            .startBlankForm("One Question Audit Track Changes")
            .clickSave()
            .fillOut(FormEntryPage.QuestionAndAnswer("What is your age?", "31"))
            .swipeToNextQuestion("What is your name?")
            .clickSave()
            .fillOut(FormEntryPage.QuestionAndAnswer("What is your name?", "Adam"))
            .swipeToEndScreen()
            .clickSaveAsDraft()

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(10))

        assertThat(auditLog[0].get("event"), equalTo("form start"))

        assertThat(auditLog[1].get("event"), equalTo("question"))
        assertThat(auditLog[1].get("node"), equalTo("/data/age"))
        assertThat(auditLog[1].get("new-value"), equalTo(""))

        assertThat(auditLog[2].get("event"), equalTo("form save"))

        assertThat(auditLog[3].get("event"), equalTo("question"))
        assertThat(auditLog[3].get("node"), equalTo("/data/age"))
        assertThat(auditLog[3].get("new-value"), equalTo("31"))

        assertThat(auditLog[4].get("event"), equalTo("question"))
        assertThat(auditLog[4].get("node"), equalTo("/data/name"))
        assertThat(auditLog[4].get("new-value"), equalTo(""))

        assertThat(auditLog[5].get("event"), equalTo("form save"))

        assertThat(auditLog[6].get("event"), equalTo("question"))
        assertThat(auditLog[6].get("node"), equalTo("/data/name"))
        assertThat(auditLog[6].get("new-value"), equalTo("Adam"))

        assertThat(auditLog[7].get("event"), equalTo("end screen"))
        assertThat(auditLog[8].get("event"), equalTo("form save"))
        assertThat(auditLog[9].get("event"), equalTo("form exit"))
    }
}
