package org.odk.collect.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.xform.parse.XFormParser
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.kxml2.kdom.Element
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage.QuestionAndAnswer
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.OkDialog
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.pages.SendFinalizedFormPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.androidtest.RecordedIntentsRule

@RunWith(AndroidJUnit4::class)
class SendFinalizedFormTest {

    private val testDependencies = TestDependencies()
    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    val chain: RuleChain = chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun canViewFormsBeforeSending() {
        rule.withProject(testDependencies.server.url)
            .copyForm("one-question.xml", testDependencies.server.hostName)
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "52"))
            .clickSendFinalizedForm(1)
            .clickOnForm("One Question")
            .assertText("52")
    }

    @Test
    fun whenThereIsAnAuthenticationError_allowsUserToReenterCredentials() {
        testDependencies.server.setCredentials("Draymond", "Green")
        rule.withProject(testDependencies.server.url)
            .copyForm("one-question.xml", testDependencies.server.hostName)
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
            .copyForm("one-question.xml", testDependencies.server.hostName)
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
            .assertText(org.odk.collect.strings.R.string.exit)
    }

    @Test
    fun canSendIndividualForms() {
        rule.withProject(testDependencies.server.url)
            .copyForm("one-question.xml", testDependencies.server.hostName)
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
            .scrollToRecyclerViewItemAndClickText(org.odk.collect.strings.R.string.delete_after_send)
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .copyForm("one-question.xml", testDependencies.server.hostName)
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
    fun whenThereAreSentAndReadyToSendForms_displayTheBanner() {
        rule.withProject(testDependencies.server.url)
            .copyForm("one-question.xml", testDependencies.server.hostName)
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "123"))
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "124"))

            .clickSendFinalizedForm(2)
            .selectForm(0)
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .assertQuantityText(org.odk.collect.strings.R.plurals.forms_ready_to_send, 1, 1)
    }

    @Test
    fun formsAreSentInOldestFirstOrderBasedOnFinalizationTime() {
        /**
         * Historically, the only timestamp we tracked for instances was the "last status changed" date.
         * However, this timestamp is updated any time the instance status changes—not only when a form
         * is finalized, but also, for example, when a submission attempt fails.
         *
         * This could lead to incorrect ordering when sending finalized forms. For instance, if forms A and B
         * were finalized in that order, and submission of form A failed, its "last status changed" timestamp
         * would be updated. As a result, when attempting to send both forms later, form B could be sent first,
         * even though form A was finalized earlier.
         *
         * To ensure that forms are always sent in the order they were finalized, we introduced a new timestamp
         * to track the finalization time specifically.
         *
         * This test reproduces the scenario described above to verify that the new finalization timestamp is used
         * for ordering. It deliberately updates the "last status changed" date of the older instance
         * to confirm that it does not affect the sending order.
         */
        testDependencies.server.alwaysReturnError()

        rule.withProject(testDependencies.server.url)
            .copyForm("one-question.xml", testDependencies.server.hostName)
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "123"))
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "124"))

            .clickSendFinalizedForm(2)
            .sortByDateOldestFirst()
            .selectForm(0)
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .also { testDependencies.server.neverReturnError() }

            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())

        val firstFormRootElement = XFormParser.getXMLDocument(testDependencies.server.submissions[0].inputStream().reader()).rootElement
        val secondFormRootElement = XFormParser.getXMLDocument(testDependencies.server.submissions[1].inputStream().reader()).rootElement

        assertThat((firstFormRootElement.getChild(0) as Element).getChild(0), equalTo("123"))
        assertThat((secondFormRootElement.getChild(0) as Element).getChild(0), equalTo("124"))
    }
}
