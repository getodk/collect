package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.strings.R

@RunWith(AndroidJUnit4::class)
class InvalidFormTest {
    private var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = chain().around(rule)

    @Test
    fun brokenForm_shouldNotBeVisibleOnFormList() {
        rule.startAtMainMenu()
            .copyForm("invalid-form.xml")
            .clickFillBlankForm()
            .checkIsSnackbarErrorVisible()
            .assertTextDoesNotExist("invalid-form")
    }

    @Test
    fun app_ShouldNotCrash_whenFillingFormsWithErrors() {
        rule.startAtMainMenu()
            .copyForm("g6Error.xml")
            .startBlankFormWithError("g6Error")
            .clickOK(FormEntryPage("g6Error"))
            .swipeToEndScreen()
            .clickFinalize()
            .checkIsSnackbarWithMessageDisplayed(R.string.form_saved)

        MainMenuPage()
            .copyForm("g6Error2.xml")
            .startBlankForm("g6Error2")
            .swipeToNextQuestionWithError()
            .clickOK(FormEntryPage("g6Error2"))
            .swipeToEndScreen()
            .clickFinalize()
            .checkIsSnackbarWithMessageDisplayed(R.string.form_saved)

        MainMenuPage()
            .copyForm("emptyGroupFieldList.xml")
            .clickFillBlankForm()
            .clickOnEmptyForm("emptyGroupFieldList")
            .clickFinalize()
            .checkIsSnackbarWithMessageDisplayed(R.string.form_saved)

        MainMenuPage()
            .copyForm("emptyGroupFieldList2.xml")
            .startBlankForm("emptyGroupFieldList2")
            .swipeToEndScreen()
            .clickFinalize()
            .checkIsSnackbarWithMessageDisplayed(R.string.form_saved)
    }
}
