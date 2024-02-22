package org.odk.collect.android.feature.formmanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

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
}
