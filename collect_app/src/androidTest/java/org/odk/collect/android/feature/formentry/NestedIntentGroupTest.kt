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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.android.material.textfield.TextInputEditText
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.rules.BlankFormTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.StringWidget
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
        val nfiqMatcher = allOf(
            isAssignableFrom(QuestionWidget::class.java),
            hasDescendant(withText(containsString("NFIQ")))
        )

        val templateMatcher = allOf(
            isAssignableFrom(QuestionWidget::class.java),
            hasDescendant(withText(containsString("Template"))))

        val resultIntent = Intent()
        resultIntent.putExtra("right_thumb_Registration_NFIQ", "2")
        resultIntent.putExtra("right_thumb_Registration_template", "foobar")

        intending(not(isInternal())).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK, resultIntent
            )
        )

        onView(withText(R.string.launch_app))
            .perform(scrollTo(), click())

        onView(allOf(isDescendantOfA(nfiqMatcher), withText("2"), isDisplayed()))
            .check(matches(not(doesNotExist())))
        onView(allOf(isDescendantOfA(templateMatcher), withText("foobar"), isDisplayed()))
            .check(matches(not(doesNotExist())))
    }

    @Test
    fun onlyTheFirstGroupWithIntentAttr_isTreatedAsIntentGroup() {
        onView(withText(R.string.launch_app)).check(matches(isDisplayed()))

        val someTextMatcher = allOf(
            isAssignableFrom(StringWidget::class.java),
            hasDescendant(withText("Some text"))
        )

        onView(allOf(isDescendantOfA(someTextMatcher), isAssignableFrom(TextInputEditText::class.java)))
            .perform(scrollTo())
            .check(matches(allOf(isDisplayed(), isEnabled())))
    }

    @Test
    fun relevance_showsAndHidesTheIntentGroup() {
        rule.startInFormEntry()
            .clickOnText("Yes")
            .assertTextDoesNotExist(R.string.launch_app)
            .assertTextsDoNotExist("* NFIQ Score")
            .assertTextsDoNotExist("Template")

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

        onView(withText(R.string.launch_app))
            .perform(scrollTo(), click())

        onView(allOf(isAssignableFrom(StringWidget::class.java),
            hasDescendant(withText(containsString("template: 6"))))).check(matches(isDisplayed()))
    }

    companion object {
        private const val INTENT_GROUP_FORM = "nested-intent-group.xml"
    }
}
