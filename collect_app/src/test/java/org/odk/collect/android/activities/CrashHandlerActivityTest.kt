package org.odk.collect.android.activities

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.crashhandler.CrashHandler
import org.odk.collect.crashhandler.hasCrashed

@RunWith(AndroidJUnit4::class)
class CrashHandlerActivityTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val crashHandler = CrashHandler.getInstance(context)!!

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Test
    fun `pressing back dismisses crash view`() {
        crashHandler.registerCrash(context, RuntimeException("BAM"))

        val scenario = launcherRule.launch(CrashHandlerActivity::class.java)
        scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        assertThat(crashHandler.hasCrashed(context), equalTo(false))
    }

    @Test
    fun `finishes if there's no crash`() {
        assertThat(launcherRule.launch(CrashHandlerActivity::class.java).state, equalTo(Lifecycle.State.DESTROYED))
    }
}
