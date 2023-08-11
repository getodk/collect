package org.odk.collect.android.support

import android.app.Activity
import android.os.Bundle
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

object ActivityControllerExtensions {
    inline fun <reified A : Activity> ActivityController<A>.recreateWithProcessRestore(
        resetProcess: () -> Unit
    ): ActivityController<A> {
        // Destroy activity with saved instance state
        val outState = Bundle()
        this.saveInstanceState(outState).pause().stop().destroy()

        // Reset process
        resetProcess()

        // Recreate with saved instance state
        return Robolectric.buildActivity(A::class.java, this.intent).setup(outState)
    }
}
