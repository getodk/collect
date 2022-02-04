package org.odk.collect.android.support;

import androidx.test.espresso.IdlingResource;

import org.odk.collect.android.support.rules.CallbackCountingTaskExecutorRule;

public class CountingTaskExecutorIdlingResource implements IdlingResource {

    private final CallbackCountingTaskExecutorRule rule;
    private ResourceCallback resourceCallback;

    public CountingTaskExecutorIdlingResource(CallbackCountingTaskExecutorRule rule) {
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
        } else {
            rule.setFinishedCallback(() -> {
                resourceCallback.onTransitionToIdle();
            });
        }

        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }
}
