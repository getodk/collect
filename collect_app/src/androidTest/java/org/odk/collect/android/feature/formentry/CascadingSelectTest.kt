package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class CascadingSelectTest {
    private var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = chain().around(rule)

    @Test
    fun cascadingSelect_withACSVFileWithColumnNamesStartingWithNumbers_shouldWorkCorrectly() {
        rule.startAtMainMenu()
            .copyForm("numberInCSV.xml", listOf("itemSets.csv"))
            .startBlankForm("numberInCSV")
            .swipeToNextQuestion("1a")
            .clickOnText("Venda de animais")
            .assertText("1a")
            .swipeToNextQuestion("2a")
            .clickOnText("Vendas agrícolas")
            .assertText("2a")
            .swipeToNextQuestion("3a")
            .clickOnText("Pensão")
            .assertText("3a")
            .swipeToNextQuestion("Thank you for taking the time to complete this form!")
            .swipeToEndScreen()
            .clickFinalize()
    }
}
