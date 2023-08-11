package org.odk.collect.testshared

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import kotlin.reflect.KClass

object EspressoHelpers {

    fun assertText(text: String) {
        onView(allOf(withText(text), withEffectiveVisibility(VISIBLE)))
            .check(matches(not(doesNotExist())))
    }

    fun clickOnContentDescription(string: Int) {
        onView(withContentDescription(string)).perform(click())
    }

    fun assertLastIntent(activityClass: KClass<*>) {
        assertThat(Intents.getIntents()[0], hasComponent(activityClass.java.name))
    }
}
