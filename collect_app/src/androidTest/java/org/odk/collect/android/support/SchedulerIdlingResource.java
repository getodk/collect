package org.odk.collect.android.support;

import androidx.test.espresso.IdlingResource;

public class SchedulerIdlingResource implements IdlingResource {

    private final TestScheduler testScheduler;
    private ResourceCallback resourceCallback;

    public SchedulerIdlingResource(TestScheduler testScheduler) {
        this.testScheduler = testScheduler;
    }

    @Override
    public String getName() {
        return SchedulerIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = testScheduler.getTaskCount() == 0;

        if (idle && resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        } else {
            testScheduler.setFinishedCallback(() -> {
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
