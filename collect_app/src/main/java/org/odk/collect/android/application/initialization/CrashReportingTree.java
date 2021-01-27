package org.odk.collect.android.application.initialization;

import android.util.Log;

import org.odk.collect.analytics.Analytics;

import timber.log.Timber;

class CrashReportingTree extends Timber.Tree {

    private final Analytics analytics;

    CrashReportingTree(Analytics analytics) {
        this.analytics = analytics;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return;
        }

        analytics.logNonFatal((priority == Log.ERROR ? "E/" : "W/") + tag + ": " + message);

        if (t != null && priority == Log.ERROR) {
            analytics.logFatal(t);
        }
    }
}
