package org.odk.collect.android.location;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

public class LocationTestRule<T extends Activity> extends ActivityTestRule<T> {
    public LocationTestRule(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
    }
}
