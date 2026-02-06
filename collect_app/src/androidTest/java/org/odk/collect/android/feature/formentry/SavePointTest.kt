package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.StorageUtils
import org.odk.collect.android.support.pages.AppClosedPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.FormHierarchyPage
import org.odk.collect.android.support.pages.SaveOrDiscardFormDialog
import org.odk.collect.android.support.rules.FormEntryActivityTestRule
import org.odk.collect.android.support.rules.ManagedComposeRule
import org.odk.collect.android.support.rules.RecentAppsRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.testshared.AssertionFramework

@RunWith(AndroidJUnit4::class)
class SavePointTest {
    private val recentAppsRule = RecentAppsRule()
    private val managedComposeRule = ManagedComposeRule()
    private val rule = FormEntryActivityTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain()
        .around(recentAppsRule)
        .around(managedComposeRule)
        .around(managedComposeRule.composeRule)
        .around(rule)

    @Test
    fun savePointIsCreatedWhenMovingForwardInForm() {
        // Create save point
        rule.setUpProjectAndCopyForm("two-question-audit.xml")
            .fillNewForm("two-question-audit.xml", "Two Question")
            .answerQuestion("What is your name?", "Alexei")
            .swipeToNextQuestion("What is your age?")
            .answerQuestion("What is your age?", "46")
            .let { simulateBatteryDeath() }

        // Start blank form and check save point is loaded
        rule.fillNewFormWithSavepoint("two-question-audit.xml")
            .clickRecover(FormHierarchyPage("Two Question"))
            .assertText("Alexei", AssertionFramework.COMPOSE)
            .assertTextDoesNotExist("46", AssertionFramework.COMPOSE)
            .pressBack(FormEntryPage("Two Question"))
            .closeSoftKeyboard()
            .assertQuestion("What is your name?")
            .pressBack(SaveOrDiscardFormDialog(AppClosedPage()))
            .clickSaveChanges()

        // Check audit log
        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(7))

        assertThat(auditLog[0].get("event"), equalTo("form start"))
        assertThat(auditLog[1].get("event"), equalTo("question"))
        // Second question event not logged - possibly a problem

        assertThat(auditLog[2].get("event"), equalTo("form resume"))
        assertThat(auditLog[3].get("event"), equalTo("jump"))
        assertThat(auditLog[4].get("event"), equalTo("question"))
        assertThat(auditLog[5].get("event"), equalTo("form save"))
        assertThat(auditLog[6].get("event"), equalTo("form exit"))
    }

    @Test
    fun whenEditing_savePointIsCreatedWhenMovingForwardInForm() {
        // Create instance
        rule.setUpProjectAndCopyForm("two-question-audit.xml", listOf("external_data_10.zip"))
            .fillNewForm("two-question-audit.xml", "Two Question")
            .fillOutAndSave(
                AppClosedPage(),
                FormEntryPage.QuestionAndAnswer("What is your name?", "Pasquale"),
                FormEntryPage.QuestionAndAnswer("What is your age?", "52")
            )

        // Create save point
        rule.editForm("two-question-audit.xml", "Two Question")
            .clickGoToStart()
            .answerQuestion("What is your name?", "Alexei")
            .swipeToNextQuestion("What is your age?")
            .answerQuestion("What is your age?", "46")
            .let { simulateBatteryDeath() }

        // Edit instance and check save point is loaded
        rule.editFormWithSavepoint("two-question-audit.xml")
            .clickRecover(FormHierarchyPage("Two Question"))
            .assertText("Alexei", AssertionFramework.COMPOSE)
            .assertText("52", AssertionFramework.COMPOSE)
            .assertTextDoesNotExist("46", AssertionFramework.COMPOSE)
            .pressBack(FormEntryPage("Two Question"))
            .closeSoftKeyboard()
            .assertQuestion("What is your name?")
            .pressBack(SaveOrDiscardFormDialog(AppClosedPage()))
            .clickSaveChanges()

        // Check audit log
        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(13))

        assertThat(auditLog[5].get("event"), equalTo("form resume"))
        assertThat(auditLog[6].get("event"), equalTo("jump"))
        assertThat(auditLog[7].get("event"), equalTo("question"))
        // Second question event not logged - possibly a problem

        assertThat(auditLog[8].get("event"), equalTo("form resume"))
        assertThat(auditLog[9].get("event"), equalTo("jump"))
        assertThat(auditLog[10].get("event"), equalTo("question"))
        assertThat(auditLog[11].get("event"), equalTo("form save"))
        assertThat(auditLog[12].get("event"), equalTo("form exit"))
    }

    @Test
    fun savePointIsCreatedWhenLeavingTheApp() {
        // Create save point
        rule.setUpProjectAndCopyForm("two-question-audit.xml", listOf("external_data_10.zip"))
            .fillNewForm("two-question-audit.xml", "Two Question")
            .answerQuestion("What is your name?", "Alexei")

        recentAppsRule.leaveAndKillApp()

        // Start blank form and check save point is loaded
        rule.fillNewFormWithSavepoint("two-question-audit.xml")
            .clickRecover(FormHierarchyPage("Two Question"))
            .assertText("Alexei", AssertionFramework.COMPOSE)
            .pressBack(FormEntryPage("Two Question"))
            .closeSoftKeyboard()
            .assertQuestion("What is your name?")
            .pressBack(SaveOrDiscardFormDialog(AppClosedPage()))
            .clickSaveChanges()

        // Check audit log
        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(6))

        assertThat(auditLog[0].get("event"), equalTo("form start"))
        // Question event not logged - possibly a problem

        assertThat(auditLog[1].get("event"), equalTo("form resume"))
        assertThat(auditLog[2].get("event"), equalTo("jump"))
        assertThat(auditLog[3].get("event"), equalTo("question"))
        assertThat(auditLog[4].get("event"), equalTo("form save"))
        assertThat(auditLog[5].get("event"), equalTo("form exit"))
    }

    @Test
    fun whenEditing_savePointIsCreatedWhenLeavingTheApp() {
        // Create instance
        rule.setUpProjectAndCopyForm("two-question-audit.xml", listOf("external_data_10.zip"))
            .fillNewForm("two-question-audit.xml", "Two Question")
            .fillOutAndSave(
                AppClosedPage(),
                FormEntryPage.QuestionAndAnswer("What is your name?", "Pasquale"),
                FormEntryPage.QuestionAndAnswer("What is your age?", "52")
            )

        // Create save point
        rule.editForm("two-question-audit.xml", "Two Question")
            .clickGoToStart()
            .answerQuestion("What is your name?", "Alexei")

        recentAppsRule.leaveAndKillApp()

        // Edit instance and check save point is loaded
        rule.editFormWithSavepoint("two-question-audit.xml")
            .clickRecover(FormHierarchyPage("Two Question"))
            .assertText("Alexei", AssertionFramework.COMPOSE)
            .assertText("52", AssertionFramework.COMPOSE)
            .pressBack(FormEntryPage("Two Question"))
            .closeSoftKeyboard()
            .assertQuestion("What is your name?")
            .pressBack(SaveOrDiscardFormDialog(AppClosedPage()))
            .clickSaveChanges()

        // Check audit log
        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(12))

        assertThat(auditLog[5].get("event"), equalTo("form resume"))
        assertThat(auditLog[6].get("event"), equalTo("jump"))
        // Question event not logged - possibly a problem

        assertThat(auditLog[7].get("event"), equalTo("form resume"))
        assertThat(auditLog[8].get("event"), equalTo("jump"))
        assertThat(auditLog[9].get("event"), equalTo("question"))
        assertThat(auditLog[10].get("event"), equalTo("form save"))
        assertThat(auditLog[11].get("event"), equalTo("form exit"))
    }

    @Test
    fun blankFormSavePointIsNotUsedWhenEditingInstance() {
        // Create instance
        rule.setUpProjectAndCopyForm("two-question-audit.xml", listOf("external_data_10.zip"))
            .fillNewForm("two-question-audit.xml", "Two Question")
            .fillOutAndSave(
                AppClosedPage(),
                FormEntryPage.QuestionAndAnswer("What is your name?", "Pasquale"),
                FormEntryPage.QuestionAndAnswer("What is your age?", "52")
            )

        // Create save point for blank form
        rule.fillNewForm("two-question-audit.xml", "Two Question")
            .answerQuestion("What is your name?", "Alexei")

        recentAppsRule.leaveAndKillApp()

        // Check editing instance doesn't load save point
        rule.editForm("two-question-audit.xml", "Two Question")
            .assertText("Pasquale", AssertionFramework.COMPOSE)
            .assertText("52", AssertionFramework.COMPOSE)
            .assertTextDoesNotExist("Alexei", AssertionFramework.COMPOSE)
    }

    @Test
    fun editedInstanceSavePointIsNotUsedWhenFillingBlankFormOfTheSameForm() {
        // Create instance
        rule.setUpProjectAndCopyForm("two-question-audit.xml", listOf("external_data_10.zip"))
            .fillNewForm("two-question-audit.xml", "Two Question")
            .fillOutAndSave(
                AppClosedPage(),
                FormEntryPage.QuestionAndAnswer("What is your name?", "Pasquale"),
                FormEntryPage.QuestionAndAnswer("What is your age?", "52")
            )

        // Create save point for instance
        rule.editForm("two-question-audit.xml", "Two Question")
            .clickGoToStart()
            .answerQuestion("What is your name?", "Alexei")

        recentAppsRule.leaveAndKillApp()

        // Check starting blank form does not load save point
        rule.fillNewForm("two-question-audit.xml", "Two Question")
    }

    @Test // https://github.com/getodk/collect/pull/6058
    fun whenBlankFormStartedThenSavedAndKilled_aSavepointShouldBeCreatedForASavedFormNotForTheBlankOne() {
        // Start blank form, save it and create a savepoint
        rule.setUpProjectAndCopyForm("two-question.xml")
            .fillNewForm("two-question.xml", "Two Question")
            .answerQuestion("What is your name?", "Alexei")
            .clickSave()
            .swipeToNextQuestion("What is your age?")
            .answerQuestion("What is your age?", "46")

        recentAppsRule.leaveAndKillApp()

        // Start blank form and check save point is not loaded
        rule.fillNewForm("two-question.xml", "Two Question")
            .pressBackAndDiscardForm(AppClosedPage())

        // Edit saved form and check save point is loaded
        rule.editFormWithSavepoint("two-question.xml")
            .clickRecover(FormHierarchyPage("Two Question"))
            .assertText("Alexei", AssertionFramework.COMPOSE)
            .assertText("46", AssertionFramework.COMPOSE)
    }

    /**
     * Simulates a case where the process is killed without lifecycle clean up (like a phone
     * being battery dying).
     */
    private fun simulateBatteryDeath(): FormEntryActivityTestRule {
        return rule.simulateProcessRestart()
    }
}
