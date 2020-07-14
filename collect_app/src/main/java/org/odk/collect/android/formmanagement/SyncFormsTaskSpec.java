package org.odk.collect.android.formmanagement;

import android.content.Context;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import javax.inject.Inject;

import timber.log.Timber;

public class SyncFormsTaskSpec implements TaskSpec {

    @Inject
    ServerFormsSynchronizer serverFormsSynchronizer;

    @Inject
    SyncStatusRepository syncStatusRepository;

    @NotNull
    @Override
    public Runnable getTask(@NotNull Context context) {
        DaggerUtils.getComponent(context).inject(this);

        return () -> {
            if (!syncStatusRepository.startSync()) {
                return;
            }

            try {
                serverFormsSynchronizer.synchronize();
            } catch (FormApiException formAPIError) {
                Timber.w(formAPIError);
            } finally {
                syncStatusRepository.finishSync();
            }
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
