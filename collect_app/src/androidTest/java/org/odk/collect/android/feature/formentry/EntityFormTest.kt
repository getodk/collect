package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class EntityFormTest {

    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun fillingFormWithEntityCreateElement_createsAnEntity() {
        rule.startAtMainMenu()
            .copyForm("one-question-entity.xml")
            .startBlankForm("One Question Entity")
            .fillOutAndSave(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))
            .openEntityBrowser()
            .clickOnDataset("people")
            .assertEntity("full_name: Logan Roy")
    }
}
