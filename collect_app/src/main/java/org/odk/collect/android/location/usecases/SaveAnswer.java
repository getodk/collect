package org.odk.collect.android.location.usecases;

import android.app.Activity;
import android.content.Intent;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.injection.config.scopes.PerActivity;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

@PerActivity
public class SaveAnswer {
    private final ActivityLogger activityLogger;
    private final Activity activity;

    @Inject
    public SaveAnswer(ActivityLogger activityLogger, Activity activity) {
        this.activityLogger = activityLogger;
        this.activity = activity;
    }

    public void save(String answer) {
        activityLogger.logInstanceAction(activity, "acceptLocation", "OK");

        Intent i = new Intent();
        i.putExtra(FormEntryActivity.LOCATION_RESULT, "");

        activity.setResult(RESULT_OK, i);
        activity.finish();
    }
}
