package org.odk.collect.android.feature.formentry

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.rules.FormEntryActivityTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

/**
 * This tests the ["Pull data from CSV" feature of XLSForms](https://xlsform.org/en/#how-to-pull-data-from-csv).
 *
 */
class DynamicPreLoadedDataPullTest {

    private val rule = FormEntryActivityTestRule()

    @get:Rule
    val copyFormChain: RuleChain = chain()
        .around(rule)

    @Test
    fun canUsePullDataFunctionToPullDataFromCSV() {
        rule.setUpProjectAndCopyForm("pull_data.xml", listOf("fruits.csv"))
            .fillNewForm("pull_data.xml", "pull_data")
            .assertText("The fruit Mango is pulled csv data.")
    }
}
