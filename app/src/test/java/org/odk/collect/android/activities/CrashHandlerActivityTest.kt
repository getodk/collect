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
import org.odk.collect.crashhandler.MockCrashView
import org.odk.collect.testshared.ActivityExt.getContextView

@RunWith(AndroidJUnit4::class)
class CrashHandlerActivityTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val crashHandler = CrashHandler.getInstance(context)!!.also {
        it.createMockViews = true
    }

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Test
    fun `pressing back dismisses crash view`() {
        crashHandler.registerCrash(context, RuntimeException("BAM"))

        launcherRule.launch(CrashHandlerActivity::class.java).onActivity {
            val crashView = it.getContextView<MockCrashView>()
            assertThat(crashView.wasDismissed, equalTo(false))

            it.onBackPressedDispatcher.onBackPressed()

            assertThat(crashView.wasDismissed, equalTo(true))
        }
    }

    /**
     * This wouldn't ideally happen, but there are scenarios where the Activity is recreated after
     * "ok" is created due to how Android's themed resources work - setting a theme that's different
     * from the system (which happens in [MainMenuActivity]) can cause even finishing Activity
     * objects to be recreated.
     */
    @Test
    fun `finishes if crash view is null`() {
        assertThat(launcherRule.launch(CrashHandlerActivity::class.java).state, equalTo(Lifecycle.State.DESTROYED))
    }
}
