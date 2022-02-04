package org.odk.collect.android.feature.external

import android.content.Intent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.external.InstancesContract
import org.odk.collect.android.support.ContentProviderUtils
import org.odk.collect.android.support.pages.AppClosedPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class FormEditActionTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun editForm_andThenFillingForm_returnsNewInstanceURI() {
        rule.startAtMainMenu()
            .copyAndSyncForm("one-question.xml")

        val formId = ContentProviderUtils.getFormDatabaseId("DEMO", "one_question")
        val uri = FormsContract.getUri("DEMO", formId)

        val formIntent = Intent(Intent.ACTION_EDIT).also { it.data = uri }
        val result = rule.launchForResult(formIntent, FormEntryPage("One Question")) {
            it.answerQuestion("what is your age", "31")
                .swipeToEndScreen()
                .clickSaveAndExit(AppClosedPage())
        }

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        assertThat(result.resultData.data, equalTo(InstancesContract.getUri("DEMO", instanceId)))
    }
}
