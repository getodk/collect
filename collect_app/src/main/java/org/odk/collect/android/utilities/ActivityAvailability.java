package org.odk.collect.android.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

public class ActivityAvailability {

    @NonNull
    private final Context context;

    public ActivityAvailability(@NonNull Context context) {
        this.context = context;
    }

    public boolean isActivityAvailable(Intent intent) {
        return context
                .getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .size() > 0;
    }
}
