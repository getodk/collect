package org.odk.collect.android.feature.formentry

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.SendFinalizedFormPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.forms.instances.Instance
import org.odk.collect.strings.R

class EncryptedFormTest {
    val testDependencies = TestDependencies()

    val rule: CollectTestRule = CollectTestRule()

    @get:Rule
    val copyFormChain: RuleChain = chain(testDependencies)
        .around(rule)

    @Test
    fun instanceOfEncryptedForm_cantBeViewedAfterFinalizing() {
        rule.startAtMainMenu()
            .copyForm("encrypted.xml")

            .startBlankForm("encrypted")
            .assertQuestion("Question 1")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickOnText("encrypted")
            .checkIsToastWithMessageDisplayed(R.string.encrypted_form)
            .assertOnPage()
    }

    @Test
    fun instanceOfEncryptedForm_cantBeViewedAfterSending() {
        rule.startAtMainMenu()
            .copyForm("encrypted.xml")
            .setServer(testDependencies.server.url)

            .startBlankForm("encrypted")
            .assertQuestion("Question 1")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

            .clickViewSentForm(1)
            .clickOnText("encrypted")
            .assertText(R.string.encrypted_form)
            .assertOnPage()
    }

    @Test
    fun instanceOfEncryptedFormWithoutInstanceID_failsFinalizationWithMessage() {
        rule.startAtMainMenu()
            .copyForm("encrypted-no-instanceID.xml")
            .startBlankForm("encrypted-no-instanceID")
            .assertQuestion("Question 1")
            .swipeToEndScreen()
            .clickFinalize()
            .checkIsToastWithMessageDisplayed("This form does not specify an instanceID. You must specify one to enable encryption. Form has not been saved as finalized.")
            .clickDrafts()
            .checkInstanceState("encrypted-no-instanceID", Instance.STATUS_INCOMPLETE)
    }

    @Test
    fun instanceOfEncryptedFormWithoutInstanceID_doesNotLeaveSavepointOnFinalization() {
        rule.startAtMainMenu()
            .copyForm("encrypted-no-instanceID.xml")
            .startBlankForm("encrypted-no-instanceID")
            .clickGoToArrow()
            .clickGoToEnd()
            .clickFinalize()
            .checkIsToastWithMessageDisplayed("This form does not specify an instanceID. You must specify one to enable encryption. Form has not been saved as finalized.")
            .startBlankForm("encrypted-no-instanceID")
    }

    @Test
    fun unicodeInEncryptedSubmissionsShouldBePreserved() {
        rule.startAtMainMenu()
            .copyForm("one-question-encrypted-unicode.xml")
            .setServer(testDependencies.server.url)

            .startBlankForm("One Question Encrypted Unicode")
            .assertQuestion("what is your age")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())

        val submission = testDependencies.server.submissions[0]
        assertThat(submission.readText().contains("one_questión_encrypted_unicode"), equalTo(true))
        assertThat(submission.readText().contains("versión 4"), equalTo(true))
    }
}
