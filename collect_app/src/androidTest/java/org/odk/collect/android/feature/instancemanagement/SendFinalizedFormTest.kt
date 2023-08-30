package org.odk.collect.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectHelpers.addGDProject
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage.QuestionAndAnswer
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.OkDialog
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.pages.SendFinalizedFormPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.projects.Project.New
import java.util.function.Supplier

@RunWith(AndroidJUnit4::class)
class SendFinalizedFormTest {

    private var currentTimeMillis: Long = System.currentTimeMillis()

    private val testDependencies = object : TestDependencies() {
        override fun providesClock(): Supplier<Long> {
            return Supplier { currentTimeMillis }
        }
    }

    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    val chain: RuleChain = chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun canEditFormsBeforeSending() {
        rule.withProject(testDependencies.server.url)
            .copyForm("one-question.xml", projectName = testDependencies.server.hostName)
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "52"))

            .clickSendFinalizedForm(1)
            .clickOnFormToEdit("One Question")
            .clickGoToStart()
            .answerQuestion("what is your age", "53")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickOnFormToEdit("One Question")
            .assertText("53")
    }

    @Test
    fun whenThereIsAnAuthenticationError_allowsUserToReenterCredentials() {
        testDependencies.server.setCredentials("Draymond", "Green")
        rule.withProject(testDependencies.server.url)
            .copyForm("one-question.xml", projectName = testDependencies.server.hostName)
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()
            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelectedWithAuthenticationError()
            .fillUsername("Draymond")
            .fillPassword("Green")
            .clickOK(OkDialog())
            .assertText("One Question - Success")
    }

    @Test
    fun canViewSentForms() {
        rule.withProject(testDependencies.server.url)
            .copyForm("one-question.xml", projectName = testDependencies.server.hostName)
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()
            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .pressBack(MainMenuPage())
            .clickViewSentForm(1)
            .clickOnForm("One Question")
            .assertText("123")
            .assertText(R.string.exit)
    }

    @Test
    fun canSendIndividualForms() {
        rule.withProject(testDependencies.server.url)
            .copyForm("one-question.xml", projectName = testDependencies.server.hostName)
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "123"))
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "124"))

            .clickSendFinalizedForm(2)
            .selectForm(0)
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

            .assertNumberOfFinalizedForms(1)
            .clickViewSentForm(1)
            .clickOnForm("One Question")
            .assertText("123")
    }

    @Test
    fun whenDeleteAfterSendIsEnabled_deletesFilledForm() {
        rule.withProject(testDependencies.server.url)
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .scrollToRecyclerViewItemAndClickText(R.string.delete_after_send)
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .copyForm("one-question.xml", projectName = testDependencies.server.hostName)
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()
            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .pressBack(MainMenuPage())
            .clickViewSentForm(1)
            .clickOnText("One Question")
            .assertOnPage()
    }

    @Test
    fun whenGoogleUsedAsServer_sendsSubmissionToSheet() {
        addGDProject(
            New(
                "GD Project",
                "G",
                "#3e9fcc"
            ),
            "dani@davey.com",
            testDependencies
        )

        rule.startAtFirstLaunch()
            .clickTryCollect()
            .openProjectSettingsDialog()
            .selectProject("GD Project")
            .copyForm("one-question-google.xml", null, false, "GD Project")
            .startBlankForm("One Question Google")
            .answerQuestion("what is your age", "47")
            .swipeToEndScreen()
            .clickFinalize()
            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .assertText("One Question Google - Success")
    }
}
