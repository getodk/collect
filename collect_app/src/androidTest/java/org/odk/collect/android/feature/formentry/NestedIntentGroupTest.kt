/*
 * Copyright 2025 ODK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.feature.formentry

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.rules.BlankFormTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.strings.R

/**
 * Tests extension to https://docs.getodk.org/launch-apps-from-collect/#launching-external-apps-to-populate-multiple-fields
 * for groups nested in field-lists.
 */
class NestedIntentGroupTest {
    var rule: BlankFormTestRule = BlankFormTestRule(INTENT_GROUP_FORM, "nested-intent-group")

    @JvmField
    @Rule
    var copyFormChain: RuleChain = chain()
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun appLaunchButton_isShownAtTopOfIntentGroup() {
        rule.startInFormEntry()
            .assertTextBelow(R.string.launch_app, "Skip?")
            .assertTextBelow("* NFIQ Score", R.string.launch_app)
    }

    @Test
    fun launchingApp_populatesValuesInIntentGroup() {
        val resultIntent = Intent()
        resultIntent.putExtra("right_thumb_Registration_NFIQ", "2")
        resultIntent.putExtra("right_thumb_Registration_template", "foobar")

        intending(not(isInternal())).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK, resultIntent
            )
        )

        rule.startInFormEntry()
            .clickOnString(R.string.launch_app)
            .assertAnswer("* NFIQ Score", "2", true)
            .assertAnswer("* Template", "foobar", true)
    }

    @Test
    fun onlyTheFirstGroupWithIntentAttr_isTreatedAsIntentGroup() {
        rule.startInFormEntry()
            .clickOnString(R.string.launch_app)
            .answerQuestion("Some text", "blah")
    }

    @Test
    fun relevance_showsAndHidesTheIntentGroup() {
        rule.startInFormEntry()
            .clickOnText("Yes")
            .assertTextDoesNotExist(R.string.launch_app)
            .assertTextsDoNotExist("* NFIQ Score")
            .assertTextsDoNotExist("* Template")

            .clickOnText("No")
            .assertTextBelow(R.string.launch_app, "Skip?")
            .assertTextBelow("* NFIQ Score", R.string.launch_app)
            .assertQuestion("Template", true)
    }

    @Test
    fun valuesFromApp_triggerRecalculation() {
        val resultIntent = Intent()
        resultIntent.putExtra("right_thumb_Registration_NFIQ", "2")
        resultIntent.putExtra("right_thumb_Registration_template", "foobar")

        intending(not(isInternal())).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK, resultIntent
            )
        )

        rule.startInFormEntry()
            .clickOnString(R.string.launch_app)
            .assertQuestion("Length of template: 6")
    }

    companion object {
        private const val INTENT_GROUP_FORM = "nested-intent-group.xml"
    }
}
