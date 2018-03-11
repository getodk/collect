package org.odk.collect.android.location.domain.actions;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.injection.scopes.PerActivity;

import javax.inject.Inject;

import io.reactivex.Completable;

import static android.app.Activity.RESULT_OK;

/**
 * @author James Knight
 */

@PerActivity
public class SetActivityResult {

    @NonNull
    private final Activity activity;

    @NonNull
    private final ActivityLogger activityLogger;

    @Inject
    SetActivityResult(@NonNull Activity activity,
                      @NonNull ActivityLogger activityLogger) {
        this.activity = activity;
        this.activityLogger = activityLogger;
    }

    Completable setAnswerForKey(@NonNull String key,
                                @NonNull String answer,
                                @NonNull String logContext,
                                @NonNull String action) {
        return Completable.defer(() -> {
            Intent i = new Intent();
            i.putExtra(key, answer);

            activity.setResult(RESULT_OK, i);
            activity.finish();

            activityLogger.logInstanceAction(activity, logContext, action);

            return Completable.complete();
        });
    }

}
