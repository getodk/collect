package org.odk.collect.androidtest

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class FakeLifecycleOwner : LifecycleOwner {

    private val lifecycle: LifecycleRegistry by lazy {
        LifecycleRegistry(this).also {
            it.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    fun destroy() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycle
    }
}
