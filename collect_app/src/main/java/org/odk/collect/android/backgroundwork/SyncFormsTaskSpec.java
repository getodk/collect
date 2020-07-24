package org.odk.collect.android.backgroundwork;

import android.content.Context;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusRepository;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import java.util.function.Supplier;

import javax.inject.Inject;

public class SyncFormsTaskSpec implements TaskSpec {

    @Inject
    ServerFormsSynchronizer serverFormsSynchronizer;

    @Inject
    SyncStatusRepository syncStatusRepository;

    @Inject
    Notifier notifier;

    @NotNull
    @Override
    public Supplier<Boolean> getTask(@NotNull Context context) {
        DaggerUtils.getComponent(context).inject(this);

        return () -> {
            if (!syncStatusRepository.startSync()) {
                return true;
            }

            try {
                serverFormsSynchronizer.synchronize();
                syncStatusRepository.finishSync(true);
            } catch (FormApiException e) {
                syncStatusRepository.finishSync(false);
                notifier.onSyncFailure(e);
            }

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
