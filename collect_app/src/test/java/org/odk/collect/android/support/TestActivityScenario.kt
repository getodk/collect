package org.odk.collect.android.support

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

/**
 * Allows the use of an [androidx.test.core.app.ActivityScenario] like interface when
 * using an Activity that isn't part of the manifest. This is useful when using a mock/fake/stub
 * Activity as part of a test
 */
class TestActivityScenario<T : Activity?> private constructor(activityClass: Class<T>) {
    private val activityController: ActivityController<T> = Robolectric.buildActivity(activityClass)

    private fun launchInternal() {
        activityController.setup()
    }

    fun onActivity(activityAction: ActivityScenario.ActivityAction<T>) {
        activityAction.perform(activityController.get())
    }

    fun recreate() {
        activityController.recreate()
    }

    companion object {
        @JvmStatic
        fun <A : Activity?> launch(activityClass: Class<A>): TestActivityScenario<A> {
            val activityScenario = TestActivityScenario(activityClass)
            activityScenario.launchInternal()
            return activityScenario
        }
    }
}
