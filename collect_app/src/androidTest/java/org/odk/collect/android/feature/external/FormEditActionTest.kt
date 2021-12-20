package org.odk.collect.android.feature.external

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.ContentProviderUtils
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.FormEntryPage

@RunWith(AndroidJUnit4::class)
class FormEditActionTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun opensForm() {
        rule.startAtMainMenu()
            .copyAndSyncForm("one-question.xml")

        val formId = ContentProviderUtils.getFormDatabaseId("DEMO", "one_question")
        val uri = FormsContract.getUri("DEMO", formId)

        val intent = Intent(Intent.ACTION_EDIT).also { it.data = uri }
        rule.launch(intent, FormEntryPage("One Question"))
    }
}
