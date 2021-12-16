package org.odk.collect.testshared

import android.app.Activity
import androidx.test.core.app.ActivityScenario

object Extensions {

    /**
     * Calling finish() doesn't seem to move an Activity to the DESTROYED state when using
     * ActivityScenario but `isFinishing` appears to work correctly.
     */
    val <T : Activity> ActivityScenario<T>.isFinishing: Boolean
        get() {
            var isFinishing = false
            this.onActivity {
                isFinishing = it.isFinishing
            }

            return isFinishing
        }
}
