package org.odk.collect.android.utilities

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class DestroyableLifecyleOwner : LifecycleOwner {
    private var lifecycleRegistry: LifecycleRegistry? = null
    fun start() {
        if (lifecycleRegistry == null) {
            lifecycleRegistry = LifecycleRegistry(this)
        }
        lifecycleRegistry!!.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun destroy() {
        if (lifecycleRegistry != null) {
            lifecycleRegistry!!.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            lifecycleRegistry = null
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry!!
    }
}
