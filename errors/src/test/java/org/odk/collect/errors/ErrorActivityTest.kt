package org.odk.collect.errors

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withListSize
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView
import java.io.Serializable

@RunWith(AndroidJUnit4::class)
class ErrorActivityTest {

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Test
    fun `list of errors should be displayed`() {
        val errors = listOf(
            ErrorItem("Title 1", "Secondary text 1", "Supporting text 1"),
            ErrorItem("Title 2", "Secondary text 2", "Supporting text 2")
        )

        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ErrorActivity::class.java).apply {
                putExtra(ErrorActivity.EXTRA_ERRORS, errors as Serializable)
            }

        launcherRule.launch<ErrorActivity>(intent)

        onView(withId(R.id.errors)).check(matches(withListSize(2)))

        onView(withRecyclerView(R.id.errors).atPositionOnView(0, R.id.title))
            .check(matches(withText("Title 1")))

        onView(withRecyclerView(R.id.errors).atPositionOnView(0, R.id.secondary_text))
            .check(matches(withText("Secondary text 1")))

        onView(withRecyclerView(R.id.errors).atPositionOnView(0, R.id.supporting_text))
            .check(matches(withText("Supporting text 1")))

        onView(withRecyclerView(R.id.errors).atPositionOnView(1, R.id.title))
            .check(matches(withText("Title 2")))

        onView(withRecyclerView(R.id.errors).atPositionOnView(1, R.id.secondary_text))
            .check(matches(withText("Secondary text 2")))

        onView(withRecyclerView(R.id.errors).atPositionOnView(1, R.id.supporting_text))
            .check(matches(withText("Supporting text 2")))
    }

    @Test
    fun `finishes when passed no errors`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ErrorActivity::class.java)

        val scenario = launcherRule.launch<ErrorActivity>(intent)
        assertThat(scenario.state, equalTo(Lifecycle.State.DESTROYED))
    }
}
