package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.StorageUtils.getAuditLogForFirstInstance
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.strings.R

@RunWith(AndroidJUnit4::class)
class RequiredQuestionTest {
    var rule: CollectTestRule = CollectTestRule()

    @get:Rule
    var ruleChain: RuleChain = chain()
        .around(rule)

    @Test
    fun requiredQuestionIsMarkedWithAnAsterisk() {
        rule.startAtMainMenu()
            .copyForm("required_question_with_custom_error_message.xml")
            .startBlankForm("required_question_with_custom_error_message")
            .assertQuestion("Required question", true)
    }

    @Test // https://github.com/getodk/collect/issues/6327
    fun requiredQuestionWithAudioIsMarkedWithAnAsterisk() {
        rule.startAtMainMenu()
            .copyForm("required_question_with_audio.xml")
            .startBlankForm("required_question_with_audio")
            .swipeToNextQuestion("Required question with audio", true)
    }

    @Test
    fun requiredQuestionDisplaysACustomErrorMessageIfSpecified() {
        rule.startAtMainMenu()
            .copyForm("required_question_with_custom_error_message.xml")
            .startBlankForm("required_question_with_custom_error_message")
            .swipeToNextQuestionWithConstraintViolation("Custom message")
    }

    @Test
    fun validatingFormByPressingValidateInOptionsMenuOnSameScreen_usesNewlyAddedAnswers() {
        rule.startAtMainMenu()
            .copyForm("required_question_with_custom_error_message.xml")
            .startBlankForm("required_question_with_custom_error_message")
            .answerQuestion("* Required question", "blah")
            .clickOptionsIcon()
            .clickOnString(R.string.validate)
            .assertText(R.string.success_form_validation)
            .assertTextDoesNotExist("Custom message")
    }

    @Test
    fun validatingFormByPressingValidateInOptionsMenuOnDifferentScreen_movesToTheQuestionWithErrorAndDisplaysError() {
        rule.startAtMainMenu()
            .copyForm("required_question_with_custom_error_message.xml")
            .startBlankForm("required_question_with_custom_error_message")
            .clickGoToArrow()
            .clickGoToEnd()
            .clickOptionsIcon()
            .clickOnString(R.string.validate)
            .assertConstraintDisplayed("Custom message")
            .assertQuestion("Required question", true)
    }

    @Test
    fun validatingFormByPressingValidateInOptionsMenuOnDifferentScreen_movesToTheQuestionWithErrorAndDisplaysError_whenTheQuestionIsInFieldList() {
        rule.startAtMainMenu()
            .copyForm("requiredQuestionInFieldList.xml")
            .startBlankForm("requiredQuestionInFieldList")
            .clickGoToArrow()
            .clickGoToEnd()
            .clickOptionsIcon()
            .clickOnString(R.string.validate)
            .assertConstraintDisplayed("Custom required message") // Make sure both questions are still displayed on the same screen
            .assertQuestion("Foo", true)
            .assertQuestion("Bar", true)
    }

    @Test
    fun emptyRequiredQuestion_isNotSavedToAuditLogOnMovingForward() {
        rule.startAtMainMenu()
            .copyForm("required_question_with_custom_error_message.xml")
            .startBlankForm("required_question_with_custom_error_message")
            .swipeToNextQuestionWithConstraintViolation("Custom message")

        val auditLog = getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(1))
        assertThat(auditLog[0][0], equalTo("form start"))
    }

    @Test
    fun emptyRequiredQuestion_isNotSavedToAuditLogOnFormValidation() {
        rule.startAtMainMenu()
            .copyForm("required_question_with_custom_error_message.xml")
            .startBlankForm("required_question_with_custom_error_message")
            .clickOptionsIcon()
            .clickOnString(R.string.validate)

        val auditLog = getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(1))
        assertThat(auditLog[0][0], equalTo("form start"))
    }

    @Test
    fun emptyRequiredQuestionInFieldListAndNotFirst_isValidatedProperly() {
        rule.startAtMainMenu()
            .copyForm("requiredQuestionInFieldList.xml")
            .startBlankForm("requiredQuestionInFieldList")
            .answerQuestion("Foo", true, "blah")
            .swipeToNextQuestionWithConstraintViolation("Custom required message2")
            .clickOptionsIcon()
            .clickOnString(R.string.validate)
            .assertText("Custom required message2")
            .clickGoToArrow()
            .clickGoToEnd()
            .clickSaveAndExitWithError("Custom required message2")
    }

    @Test // https://github.com/getodk/collect/issues/5847
    fun savingFormWithInvalidQuestion_doesNotChangeTheCurrentQuestionIndex() {
        rule.startAtMainMenu()
            .copyForm("two-question-required.xml")
            .startBlankForm("Two Question Required")
            .clickSave()
            .swipeToNextQuestion("What is your age?")
    }
}
