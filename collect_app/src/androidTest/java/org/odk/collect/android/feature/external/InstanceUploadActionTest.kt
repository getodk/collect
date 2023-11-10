package org.odk.collect.android.feature.external

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.external.InstancesContract
import org.odk.collect.android.instancemanagement.send.InstanceUploaderActivity
import org.odk.collect.android.support.pages.OkDialog
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class InstanceUploadActionTest {

    val collectTestRule = CollectTestRule()

    @get:Rule
    val rule: RuleChain = TestRuleChain.chain()
        .around(collectTestRule)

    @Test
    fun whenInstanceDoesNotExist_showsError() {
        val instanceIds = longArrayOf(11)
        instanceUploadAction(instanceIds)

        OkDialog()
            .assertOnPage()
            .assertText(org.odk.collect.strings.R.string.no_forms_uploaded)
    }

    private fun instanceUploadAction(instanceIds: LongArray) {
        val intent = Intent("org.odk.collect.android.INSTANCE_UPLOAD")
        intent.type = InstancesContract.CONTENT_TYPE
        intent.putExtra("instances", instanceIds)
        collectTestRule.launch<InstanceUploaderActivity>(intent)
    }
}
