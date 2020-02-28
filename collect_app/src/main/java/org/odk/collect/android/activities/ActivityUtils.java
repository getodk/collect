package org.odk.collect.android.activities;

import android.app.Activity;
import android.content.Intent;

public class ActivityUtils {

    private ActivityUtils() {

    }

    public static void startActivityAndCloseAllOthers(Activity activity, Class<MainMenuActivity> activityClass) {
        activity.startActivity(new Intent(activity, activityClass));
        activity.overridePendingTransition(0, 0);
        activity.finishAffinity();
    }
}
