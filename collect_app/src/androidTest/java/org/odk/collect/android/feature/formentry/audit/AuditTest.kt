package org.odk.collect.android.feature.formentry.audit

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.StorageUtils
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class AuditTest {

    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun fillAndEditingForm_updatesAuditLogForInstance() {
        rule.startAtMainMenu()
            .copyForm("one-question-audit.xml")
            .startBlankForm("One Question Audit")
            .fillOutAndSave(
                FormEntryPage.QuestionAndAnswer("what is your age", "31")
            )
            .clickEditSavedForm(1)
            .clickOnForm("One Question Audit")
            .clickGoToStart()
            .fillOutAndSave(
                FormEntryPage.QuestionAndAnswer("what is your age", "32")
            )

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(13))

        assertThat(auditLog[0].get("event"), equalTo("form start"))
        assertThat(auditLog[1].get("event"), equalTo("question"))
        assertThat(auditLog[2].get("event"), equalTo("end screen"))
        assertThat(auditLog[3].get("event"), equalTo("form save"))
        assertThat(auditLog[4].get("event"), equalTo("form exit"))
        assertThat(auditLog[5].get("event"), equalTo("form finalize"))

        assertThat(auditLog[6].get("event"), equalTo("form resume"))
        assertThat(auditLog[7].get("event"), equalTo("jump"))
        assertThat(auditLog[8].get("event"), equalTo("question"))
        assertThat(auditLog[9].get("event"), equalTo("end screen"))
        assertThat(auditLog[10].get("event"), equalTo("form save"))
        assertThat(auditLog[11].get("event"), equalTo("form exit"))
        assertThat(auditLog[12].get("event"), equalTo("form finalize"))
    }
}
