package org.odk.collect.android.feature.external

import android.content.Intent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.external.InstancesContract
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.ContentProviderUtils
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.EditSavedFormPage

@RunWith(AndroidJUnit4::class)
class InstancePickActionTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun pickInstance_andTheSelectingInstance_returnsInstanceUri() {
        rule.startAtMainMenu()
            .copyAndSyncForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAndExit()

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = InstancesContract.CONTENT_TYPE
        val result = rule.launchForResult(intent, EditSavedFormPage()) {
            it.clickOnForm("One Question")
        }

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        assertThat(
            result.resultData.data,
            equalTo(InstancesContract.getUri("DEMO", instanceId))
        )
    }
}
