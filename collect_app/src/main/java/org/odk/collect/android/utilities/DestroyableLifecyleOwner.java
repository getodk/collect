package org.odk.collect.android.utilities;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import static androidx.lifecycle.Lifecycle.Event.ON_DESTROY;
import static androidx.lifecycle.Lifecycle.Event.ON_RESUME;

public class DestroyableLifecyleOwner implements LifecycleOwner {

    private LifecycleRegistry lifecycleRegistry;

    public void start() {
        if (lifecycleRegistry == null) {
            lifecycleRegistry = new LifecycleRegistry(this);
        }

        lifecycleRegistry.handleLifecycleEvent(ON_RESUME);
    }

    public void destroy() {
        if (lifecycleRegistry != null) {
            lifecycleRegistry.handleLifecycleEvent(ON_DESTROY);
            lifecycleRegistry = null;
        }
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }
}
