package org.odk.collect.android.support;

import androidx.arch.core.executor.testing.CountingTaskExecutorRule;
import androidx.test.espresso.IdlingResource;

public class CountingTaskExecutorIdlingResource extends CountingTaskExecutorRule implements IdlingResource {

    private IdlingResource.ResourceCallback resourceCallback;

    @Override
    public String getName() {
        return CountingTaskExecutorIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        return isIdle();
    }

    @Override
    public void registerIdleTransitionCallback(IdlingResource.ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

    @Override
    protected void onIdle() {
        resourceCallback.onTransitionToIdle();
    }
}
