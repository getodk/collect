package org.odk.collect.testshared

import android.content.Intent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import org.hamcrest.Matcher
import org.odk.collect.testshared.EspressoHelpers.assertIntents
import kotlin.reflect.KClass

class AssertIntentsHelper {

    private val matchers = mutableListOf<Matcher<Intent>>()

    fun assertNewIntent(matcher: Matcher<Intent>) {
        matchers.add(matcher)
        assertIntents(*matchers.toTypedArray())
    }

    fun assertNewIntent(activityClass: KClass<*>) {
        matchers.add(hasComponent(activityClass.java.name))
        assertIntents(*matchers.toTypedArray())
    }

    fun assertNoNewIntent() {
        assertIntents(*matchers.toTypedArray())
    }
}
