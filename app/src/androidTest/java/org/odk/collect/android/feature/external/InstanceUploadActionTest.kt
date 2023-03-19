package org.odk.collect.android.feature.external

import android.content.Intent
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.activities.InstanceUploaderActivity
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
            .assertText(R.string.no_forms_uploaded)
    }

    private fun instanceUploadAction(instanceIds: LongArray) {
        /*
        This should really use `Intent(action: String)` but this seems to be broken right now:
        https://github.com/android/android-test/issues/496
         */
        val intent = Intent(getApplicationContext(), InstanceUploaderActivity::class.java)
        intent.putExtra("instances", instanceIds)
        collectTestRule.launch<InstanceUploaderActivity>(intent)
    }
}
