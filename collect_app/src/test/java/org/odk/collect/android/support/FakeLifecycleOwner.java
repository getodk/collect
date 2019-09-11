package org.odk.collect.android.support;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

public class FakeLifecycleOwner implements LifecycleOwner {

    private final LifecycleRegistry lifecycle = new LifecycleRegistry(this);

    public FakeLifecycleOwner() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    public void destroy() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycle;
    }
}
