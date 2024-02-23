package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.strings.R

@RunWith(AndroidJUnit4::class)
class FormAppearancesTest {
    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun searchAppearance_ShouldDisplayWhenSearchAppearanceIsSpecified() {
        rule.startAtMainMenu()
            .copyForm("different-search-appearances.xml", listOf("fruits.csv"))
            .startBlankForm("different-search-appearances")
            .clickOnText("Mango")
            .swipeToNextQuestion("The fruit mango pulled from csv")
            .assertText("The fruit mango pulled from csv")
            .swipeToNextQuestion("Static select with no appearance")
            .clickOnText("Wolf")
            .swipeToNextQuestion("Static select with search appearance")
            .inputText("w")
            .closeSoftKeyboard()
            .assertText("Wolf")
            .assertText("Warthog")
            .clickOnText("Wolf")
            .swipeToNextQuestion("Static select with autocomplete appearance")
            .inputText("r")
            .closeSoftKeyboard()
            .assertText("Warthog")
            .assertText("Raccoon")
            .assertText("Rabbit")
            .closeSoftKeyboard()
            .clickOnText("Rabbit")
            .swipeToNextQuestion("Select from a CSV using search() appearance/function and search appearance")
            .inputText("r")
            .closeSoftKeyboard()
            .assertText("Oranges")
            .assertText("Strawberries")
            .clickOnText("Oranges")
            .swipeToNextQuestion("Select from a CSV using search() appearance/function and autocomplete appearance")
            .inputText("n")
            .closeSoftKeyboard()
            .assertText("Mango")
            .assertText("Oranges")
            .clickOnText("Mango")
            .swipeToNextQuestion("Select from a CSV using search() appearance/function")
            .clickOnText("Mango")
            .clickOnText("Strawberries")
            .swipeToNextQuestion("Static select with no appearance")
            .clickOnText("Raccoon")
            .clickOnText("Rabbit")
            .swipeToNextQuestion("Static select with search appearance")
            .inputText("w")
            .closeSoftKeyboard()
            .assertText("Wolf")
            .assertText("Warthog")
            .clickOnText("Wolf")
            .clickOnText("Warthog")
            .swipeToNextQuestion("Static select with autocomplete appearance")
            .inputText("r")
            .closeSoftKeyboard()
            .assertText("Warthog")
            .assertText("Raccoon")
            .assertText("Rabbit")
            .clickOnText("Raccoon")
            .clickOnText("Rabbit")
            .swipeToNextQuestion("Select from a CSV using search() appearance/function and search appearance")
            .inputText("m")
            .closeSoftKeyboard()
            .assertText("Mango")
            .clickOnText("Mango")
            .swipeToNextQuestion("Select from a CSV using search() appearance/function and autocomplete appearance")
            .inputText("n")
            .closeSoftKeyboard()
            .closeSoftKeyboard()
            .assertText("Mango")
            .assertText("Oranges")
            .clickOnText("Mango")
            .clickOnText("Oranges")
            .swipeToEndScreen()
            .clickFinalize()
            .checkIsSnackbarWithMessageDisplayed(R.string.form_saved)
    }
}
