package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class EntityFormLockingTest {

    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun closingEntityForm_releasesTheLockAndLetsOtherEntityFormsToBeStarted() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("one-question-entity-registration.xml")
            .startBlankForm("One Question Entity Registration")
            .pressBackAndDiscardForm()
            .startBlankForm("One Question Entity Registration")
    }

    @Test
    fun closingBrokenEntityForm_releasesTheLockAndLetsOtherEntityFormsToBeStarted() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("one-question-entity-registration-broken.xml")
            .copyForm("one-question-entity-registration.xml")
            .startBlankFormWithError("One Question Entity Registration Broken", true)
            .clickOKOnDialog(MainMenuPage())
            .startBlankForm("One Question Entity Registration")
    }
}
