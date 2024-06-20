package org.odk.collect.testshared

import android.content.Intent
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo

object Assertions {

    fun assertText(view: Matcher<View>, root: Matcher<Root>? = null) {
        val onView = if (root != null) {
            onView(allOf(view, withEffectiveVisibility(VISIBLE))).inRoot(root)
        } else {
            onView(allOf(view, withEffectiveVisibility(VISIBLE)))
        }

        onView.check(matches(not(doesNotExist())))
    }

    fun assertIntents(vararg intentMatchers: Matcher<Intent>) {
        val intents = Intents.getIntents()
        assertThat(intentMatchers.size, equalTo(intents.size))

        intentMatchers.forEachIndexed { index, matcher ->
            assertThat(intents[index], matcher)
        }
    }
}
