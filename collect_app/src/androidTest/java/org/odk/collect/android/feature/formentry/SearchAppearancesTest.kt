package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class SearchAppearancesTest {
    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun searchFunctionFetchesChoicesForSelectOneFromCSVFile() {
        rule.startAtMainMenu()
            .copyForm("different-search-appearances.xml", listOf("fruits.csv"))
            .startBlankForm("different-search-appearances")
            .assertTexts("Mango", "Oranges", "Strawberries")
    }

    @Test
    fun searchAppearanceEnablesFilteringChoicesForSelectOne() {
        rule.startAtMainMenu()
            .copyForm("different-search-appearances.xml", listOf("fruits.csv"))
            .startBlankForm("different-search-appearances")
            .clickGoToArrow()
            .clickOnQuestion("Static select one with search appearance")
            .inputText("w")
            .closeSoftKeyboard()
            .assertTexts("Wolf", "Warthog")
            .assertTextsDoNotExist("Racoon", "Rabbit")
    }

    @Test
    fun autocompleteAppearanceEnablesFilteringChoicesForSelectOne() {
        rule.startAtMainMenu()
            .copyForm("different-search-appearances.xml", listOf("fruits.csv"))
            .startBlankForm("different-search-appearances")
            .clickGoToArrow()
            .clickOnQuestion("Static select one with autocomplete appearance")
            .inputText("r")
            .closeSoftKeyboard()
            .assertTexts("Warthog", "Raccoon", "Rabbit")
            .assertTextDoesNotExist("Wolf")
    }

    @Test
    fun searchFunctionCanBeCombinedWithSearchAppearanceForSelectOne() {
        rule.startAtMainMenu()
            .copyForm("different-search-appearances.xml", listOf("fruits.csv"))
            .startBlankForm("different-search-appearances")
            .clickGoToArrow()
            .clickOnQuestion("Select one from a CSV using search() appearance/function and search appearance")
            .inputText("r")
            .closeSoftKeyboard()
            .assertTexts("Oranges", "Strawberries")
            .assertTextDoesNotExist("Mango")
    }

    @Test
    fun searchFunctionCanBeCombinedWithAutocompleteAppearanceForSelectOne() {
        rule.startAtMainMenu()
            .copyForm("different-search-appearances.xml", listOf("fruits.csv"))
            .startBlankForm("different-search-appearances")
            .clickGoToArrow()
            .clickOnQuestion("Select one from a CSV using search() appearance/function and autocomplete appearance")
            .inputText("n")
            .closeSoftKeyboard()
            .assertTexts("Mango", "Oranges")
            .assertTextDoesNotExist("Strawberries")
    }

    @Test
    fun searchFunctionFetchesChoicesForSelectMultipleFromCSVFile() {
        rule.startAtMainMenu()
            .copyForm("different-search-appearances.xml", listOf("fruits.csv"))
            .startBlankForm("different-search-appearances")
            .clickGoToArrow()
            .clickOnQuestion("Select multiple from a CSV using search() appearance/function")
            .assertTexts("Mango", "Oranges", "Strawberries")
    }

    @Test
    fun searchAppearanceEnablesFilteringChoicesForSelectMultiple() {
        rule.startAtMainMenu()
            .copyForm("different-search-appearances.xml", listOf("fruits.csv"))
            .startBlankForm("different-search-appearances")
            .clickGoToArrow()
            .clickOnQuestion("Static select multiple with search appearance")
            .inputText("w")
            .closeSoftKeyboard()
            .assertTexts("Wolf", "Warthog")
            .assertTextsDoNotExist("Racoon", "Rabbit")
    }

    @Test
    fun autocompleteAppearanceEnablesFilteringChoicesForSelectMultiple() {
        rule.startAtMainMenu()
            .copyForm("different-search-appearances.xml", listOf("fruits.csv"))
            .startBlankForm("different-search-appearances")
            .clickGoToArrow()
            .clickOnQuestion("Static select multiple with autocomplete appearance")
            .inputText("r")
            .closeSoftKeyboard()
            .assertTexts("Warthog", "Raccoon", "Rabbit")
            .assertTextDoesNotExist("Wolf")
    }

    @Test
    fun searchFunctionCanBeCombinedWithSearchAppearanceForSelectMultiple() {
        rule.startAtMainMenu()
            .copyForm("different-search-appearances.xml", listOf("fruits.csv"))
            .startBlankForm("different-search-appearances")
            .clickGoToArrow()
            .clickOnQuestion("Select multiple from a CSV using search() appearance/function and search appearance")
            .inputText("r")
            .closeSoftKeyboard()
            .assertTexts("Oranges", "Strawberries")
            .assertTextDoesNotExist("Mango")
    }

    @Test
    fun searchFunctionCanBeCombinedWithAutocompleteAppearanceForSelectMultiple() {
        rule.startAtMainMenu()
            .copyForm("different-search-appearances.xml", listOf("fruits.csv"))
            .startBlankForm("different-search-appearances")
            .clickGoToArrow()
            .clickOnQuestion("Select multiple from a CSV using search() appearance/function and autocomplete appearance")
            .inputText("n")
            .closeSoftKeyboard()
            .assertTexts("Mango", "Oranges")
            .assertTextDoesNotExist("Strawberries")
    }
}
