package org.odk.collect.android.location.domain;


import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.scopes.ActivityScope;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

@ActivityScope
public class SaveLocation {

    @NonNull
    private final Activity activity;

    @NonNull
    private final LocationFormatter locationFormatter;

    public boolean isFullLocation = false;

    @Inject
    public SaveLocation(@NonNull Activity activity,
                        @NonNull LocationFormatter locationFormatter) {
        this.activity = activity;
        this.locationFormatter = locationFormatter;
    }

    public void save(@Nullable Location location) {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "acceptLocation",
                "OK");

        String result;
        // setClear || (readOnly && latLng == null)
        if (location == null) {
            result = "";


        }
        // isDragged || readOnly || locationFromIntent
        else if (isFullLocation) {
            result = locationFormatter.formatForData(location.getLatitude(), location.getLongitude(), 0, 0);

        }
        // location != null
        else {
            result = locationFormatter.formatForLocation(location);
        }

        Intent intent = new Intent();
        intent.putExtra(FormEntryActivity.LOCATION_RESULT, result);

        activity.setResult(RESULT_OK, intent);
        activity.finish();
    }
}
