package org.odk.collect.android.support;

import androidx.arch.core.executor.testing.CountingTaskExecutorRule;
import androidx.test.espresso.IdlingResource;

public class CountingTaskExecutorIdlingResource implements IdlingResource {

    private final CountingTaskExecutorRule rule;
    private ResourceCallback resourceCallback;

    public CountingTaskExecutorIdlingResource(CountingTaskExecutorRule rule) {
        this.rule = rule;
    }

    @Override
    public String getName() {
        return CountingTaskExecutorIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = rule.isIdle();
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
