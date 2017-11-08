package org.odk.collect.android;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DependencyProvider;
import org.odk.collect.android.utilities.ActivityAvailability;

public class TestCollect extends Collect implements DependencyProvider<ActivityAvailability> {

    private ActivityAvailability activityAvailability = new ActivityAvailability(this);

    @Override
    public ActivityAvailability provide() {
        return activityAvailability;
    }

    public void setActivityAvailability(ActivityAvailability activityAvailability) {
        this.activityAvailability = activityAvailability;
    }
}
