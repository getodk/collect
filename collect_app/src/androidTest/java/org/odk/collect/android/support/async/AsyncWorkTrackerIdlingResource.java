package org.odk.collect.android.support.async;

import androidx.test.espresso.IdlingResource;

public class AsyncWorkTrackerIdlingResource implements IdlingResource {

    private ResourceCallback resourceCallback;

    @Override
    public String getName() {
        return AsyncWorkTrackerIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = AsyncWorkTracker.getTaskCount() == 0;
        if (idle && resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        }

        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }
}
