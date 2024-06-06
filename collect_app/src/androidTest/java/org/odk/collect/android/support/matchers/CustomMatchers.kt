/*
 * Copyright 2019 Nafundi
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
package org.odk.collect.android.support.matchers

import android.view.View
import android.widget.TextView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.odk.collect.android.R
import org.odk.collect.android.widgets.QuestionWidget

/**
 * Grab bag of Hamcrest matchers.
 */
object CustomMatchers {
    /**
     * Matches the view at the given index. Useful when several views have the same properties.
     * https://stackoverflow.com/a/39756832
     *
     */
    @Deprecated("this matcher is stateful and will cause problems if used more than once")
    @JvmStatic
    fun withIndex(matcher: Matcher<View>, index: Int): TypeSafeMatcher<View> {
        return object : TypeSafeMatcher<View>() {
            var currentIndex: Int = 0

            override fun describeTo(description: Description) {
                description.appendText("with index: ")
                description.appendValue(index)
                matcher.describeTo(description)
            }

            override fun matchesSafely(view: View): Boolean {
                return matcher.matches(view) && currentIndex++ == index
            }
        }
    }

    @JvmStatic
    fun isQuestionView(questionText: String): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("is question view with text: ")
                description.appendValue(questionText)
            }

            override fun matchesSafely(item: View): Boolean {
                return if (item is QuestionWidget) {
                    val questionTextView = item.findViewById<TextView>(R.id.text_label)
                    questionTextView.text.toString() == questionText
                } else {
                    false
                }
            }
        }
    }
}
