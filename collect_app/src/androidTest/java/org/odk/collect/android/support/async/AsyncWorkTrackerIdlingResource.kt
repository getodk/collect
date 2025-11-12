package org.odk.collect.android.support.async

import androidx.test.espresso.IdlingResource
import org.odk.collect.android.support.async.AsyncWorkTracker.taskCount

class AsyncWorkTrackerIdlingResource : IdlingResource {

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String {
        return AsyncWorkTrackerIdlingResource::class.java.name
    }

    override fun isIdleNow(): Boolean {
        val idle = taskCount == 0
        if (idle) {
            resourceCallback?.onTransitionToIdle()
        }

        return idle
    }

    override fun registerIdleTransitionCallback(resourceCallback: IdlingResource.ResourceCallback?) {
        this.resourceCallback = resourceCallback
    }
}
