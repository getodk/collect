package org.odk.collect.android.application.initialization;

import android.util.Log;
import androidx.annotation.NonNull;
import org.odk.collect.analytics.Analytics;
import timber.log.Timber;

class CrashReportingTree extends Timber.Tree {

    private final Analytics analytics;

    CrashReportingTree(Analytics analytics) {
        this.analytics = analytics;
    }

    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return;
        }

        if (priority == Log.ERROR) {
            Throwable throwable = t != null ? t : new Throwable("E/" + tag + ": " + message);
            analytics.logNonFatal(throwable);
        }

        if (priority == Log.WARN) {
            analytics.logMessage("W/" + tag + ": " + message);
        }
    }
}
