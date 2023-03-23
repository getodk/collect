package org.odk.collect.androidtest

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class FakeLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry: LifecycleRegistry by lazy {
        LifecycleRegistry(this).also {
            it.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    override val lifecycle: LifecycleRegistry = lifecycleRegistry

    fun destroy() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}
