package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.rules.FormEntryActivityTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class FormMediaTest {

    private val rule = FormEntryActivityTestRule()

    @get:Rule
    val ruleChain: RuleChain = chain()
        .around(rule)

    @Test
    fun loadingFormWithZippedMedia_unzipsIntoMediaDirectory() {
        rule.setUpProjectAndCopyForm("external_select_10.xml", listOf("external_data_10.zip"))
            .fillNewForm("external_select_10.xml", "external select 10")
            .clickOnText("a")
            .swipeToNextQuestion("Second")
            .assertText("aa")
            .assertText("ab")
            .assertText("ac")
    }
}
