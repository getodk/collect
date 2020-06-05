package org.odk.collect.android.support;

import androidx.test.espresso.IdlingResource;

public class CountingSchedulerIdlingResource implements IdlingResource {

    private final CountingScheduler countingScheduler;
    private ResourceCallback resourceCallback;

    public CountingSchedulerIdlingResource(CountingScheduler countingScheduler) {
        this.countingScheduler = countingScheduler;
    }

    @Override
    public String getName() {
        return CountingSchedulerIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = countingScheduler.getTaskCount() == 0;

        if (idle && resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        } else {
            countingScheduler.setFinishedCallback(() -> {
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
