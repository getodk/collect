package org.odk.collect.android.utilities

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class ControllableLifecyleOwner : LifecycleOwner {
    private var lifecycleRegistry = LifecycleRegistry(this).apply {
        this.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override val lifecycle: LifecycleRegistry = lifecycleRegistry

    fun start() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun destroy() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}
