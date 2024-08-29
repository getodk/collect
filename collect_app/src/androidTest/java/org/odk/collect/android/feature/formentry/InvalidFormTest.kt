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
            .checkIsSnackbarErrorVisible("org.javarosa.xform.parse.XFormParseException: Cycle detected in form's relevant and calculation logic!")
            .assertTextDoesNotExist("invalid-form")
    }

    @Test
    fun app_ShouldNotCrash_whenFillingFormsWithEmptyGroupFieldList() {
        MainMenuPage()
            .copyForm("emptyGroupFieldList.xml")
            .clickFillBlankForm()
            .clickOnEmptyForm("emptyGroupFieldList")
            .clickFinalize()
            .checkIsSnackbarWithMessageDisplayed(R.string.form_saved)
    }

    @Test
    fun app_ShouldNotCrash_whenFillingFormsWithRepeatInFieldList() {
        rule.startAtMainMenu()
            .copyForm("repeat_in_field_list.xml")
            .startBlankFormWithError("repeat_in_field_list")
            .clickOK(FormEntryPage("repeat_in_field_list"))
            .swipeToEndScreen()
            .clickFinalize()
            .checkIsSnackbarWithMessageDisplayed(R.string.form_saved)
    }
}
