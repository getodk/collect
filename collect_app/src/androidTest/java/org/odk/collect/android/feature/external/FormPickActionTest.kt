package org.odk.collect.android.feature.external

import android.content.Intent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.ContentProviderUtils
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.FillBlankFormPage

@RunWith(AndroidJUnit4::class)
class FormPickActionTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun pickForm_andTheSelectingForm_returnsFormUri() {
        rule.startAtMainMenu()
            .copyAndSyncForm("one-question.xml")

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = FormsContract.CONTENT_TYPE
        val result = rule.launchForResult(intent, FillBlankFormPage()) {
            it.clickOnForm("One Question")
        }

        val formId = ContentProviderUtils.getFormDatabaseId("DEMO", "one_question")
        assertThat(result.resultData.data, equalTo(FormsContract.getUri("DEMO", formId)))
    }
}
