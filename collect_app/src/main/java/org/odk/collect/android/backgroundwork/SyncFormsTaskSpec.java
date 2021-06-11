package org.odk.collect.android.backgroundwork;

import android.content.Context;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.formmanagement.FormsUpdater;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;

public class SyncFormsTaskSpec implements TaskSpec {

    public static final String DATA_PROJECT_ID = "projectId";

    @Inject
    FormsUpdater formsUpdater;

    @NotNull
    @Override
    public Supplier<Boolean> getTask(@NotNull Context context, @NotNull Map<String, String> inputData) {
        DaggerUtils.getComponent(context).inject(this);

        return () -> {
            formsUpdater.matchFormsWithServer(inputData.get(DATA_PROJECT_ID));
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
