package org.odk.collect.audiorecorder.recording.internal

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReturnToAppActivityTest {

    @Test
    fun finishesImmediately() {
        val scenario = launchActivity<ReturnToAppActivity>()
        assertThat(scenario.state, equalTo(Lifecycle.State.DESTROYED))
    }
}
