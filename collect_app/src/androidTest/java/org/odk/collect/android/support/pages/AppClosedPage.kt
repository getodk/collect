package org.odk.collect.android.support.pages

import android.app.Activity
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

class AppClosedPage : Page<AppClosedPage>() {

    override fun assertOnPage(): AppClosedPage {
        assertThat(currentActivity, equalTo(null))
        return this
    }

    private val currentActivity: Activity?
        get() {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            val activity = arrayOfNulls<Activity>(1)
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                val activities =
                    ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(
                        Stage.RESUMED
                    )
                if (!activities.isEmpty()) {
                    activity[0] = Iterables.getOnlyElement(activities) as Activity
                } else {
                    activity[0] = null
                }
            }
            return activity[0]
        }
}
