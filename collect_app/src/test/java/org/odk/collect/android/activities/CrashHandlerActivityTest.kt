package org.odk.collect.android.activities

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.crash_handler.CrashHandler
import org.odk.collect.crash_handler.MockCrashView
import org.odk.collect.testshared.ActivityExt.getContextView

@RunWith(AndroidJUnit4::class)
class CrashHandlerActivityTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Test
    fun `pressing back dismisses crash view`() {
        CrashHandler.getInstance(context)!!.also {
            it.createMockViews = true
            it.registerCrash(context, RuntimeException("BAM"))
        }

        launcherRule.launch(CrashHandlerActivity::class.java).onActivity {
            val crashView = it.getContextView<MockCrashView>()
            assertThat(crashView.wasDismissed, equalTo(false))

            it.onBackPressedDispatcher.onBackPressed()

            assertThat(crashView.wasDismissed, equalTo(true))
        }
    }
}
