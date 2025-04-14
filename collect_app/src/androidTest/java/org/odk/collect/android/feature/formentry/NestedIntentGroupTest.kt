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

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.android.material.textfield.TextInputEditText
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.rules.BlankFormTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.android.widgets.DecimalWidget
import org.odk.collect.android.widgets.IntegerWidget
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
    fun appLaunchButtonIsShown() {
        onView(withText(R.string.launch_app)).check(isCompletelyAbove(withText(containsString("NFIQ"))))
        onView(withText(R.string.launch_app)).check(isCompletelyBelow(withText(containsString("Skip?"))))
    }

    @Test
    fun onlyTheFirstGroupWithIntentAttr__isTreatedAsIntentGroup() {
        val secondGroupTextFieldMatcher = CoreMatchers.allOf(
            isDescendantOfA(withText("Some text")),
            isDescendantOfA(
                CoreMatchers.allOf(
                    isAssignableFrom(StringWidget::class.java),
                    CoreMatchers.not(isAssignableFrom(IntegerWidget::class.java)),
                    CoreMatchers.not(isAssignableFrom(DecimalWidget::class.java))
                )
            ),
            isAssignableFrom(TextInputEditText::class.java))

        onView(secondGroupTextFieldMatcher).perform(scrollTo())
            .check(matches(isDisplayed()))

        onView(secondGroupTextFieldMatcher).perform()
            .check(matches(isEnabled()))
    }

    companion object {
        private const val INTENT_GROUP_FORM = "nested-intent-group.xml"
    }
}
