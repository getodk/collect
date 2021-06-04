package org.odk.collect.android.backgroundwork;

import android.content.Context;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.formmanagement.FormUpdateChecker;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;

public class SyncFormsTaskSpec implements TaskSpec {

    @Inject
    SyncStatusAppState syncStatusAppState;

    @Inject
    Notifier notifier;

    @Inject
    @Named("FORMS")
    ChangeLock changeLock;

    @Inject
    Analytics analytics;

    @Inject
    FormUpdateChecker formUpdateChecker;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    @NotNull
    @Override
    public Supplier<Boolean> getTask(@NotNull Context context, @NotNull Map<String, String> inputData) {
        DaggerUtils.getComponent(context).inject(this);

        return () -> {
            formUpdateChecker.synchronizeWithServer();
            return true;
        };
    }

    @NotNull
    @Override
    public Class<? extends WorkerAdapter> getWorkManagerAdapter() {
        return Adapter.class;
    }

    public static class Adapter extends WorkerAdapter {

        public Adapter(@NotNull Context context, @NotNull WorkerParameters workerParams) {
            super(new SyncFormsTaskSpec(), context, workerParams);
        }
    }
}
