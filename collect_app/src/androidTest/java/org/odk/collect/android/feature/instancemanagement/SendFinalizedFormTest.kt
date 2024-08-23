package org.odk.collect.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.kxml2.io.KXmlParser
import org.kxml2.kdom.Document
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
import java.io.File
import java.io.StringReader

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
    fun formsAreSentInOldestFirstOrder() {
        rule.withProject(testDependencies.server.url)
            .copyForm("one-question.xml", testDependencies.server.hostName)
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "123"))
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "124"))

            .clickSendFinalizedForm(2)
            .selectForm(1)
            .selectForm(0)
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())

        val root1 = parseXml(testDependencies.server.submissions[0]).rootElement
        val root2 = parseXml(testDependencies.server.submissions[1]).rootElement

        assertThat((root1.getChild(0) as Element).getChild(0), equalTo("123"))
        assertThat((root2.getChild(0) as Element).getChild(0), equalTo("124"))
    }

    private fun parseXml(file: File): Document {
        return StringReader(String(file.readBytes())).use { reader ->
            val parser = KXmlParser()
            parser.setInput(reader)
            Document().also { it.parse(parser) }
        }
    }
}
