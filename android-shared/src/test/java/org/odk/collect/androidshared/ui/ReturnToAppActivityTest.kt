package org.odk.collect.androidshared.ui

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReturnToAppActivityTest {

    @get:Rule
    val rule = ActivityScenarioRule(ReturnToAppActivity::class.java)

    @Test
    fun finishesImmediately() {
        assertThat(rule.scenario.state, equalTo(Lifecycle.State.DESTROYED))
    }
}
