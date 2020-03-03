package org.odk.collect.android.storage.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class FakedAutoSendWorker extends Worker {

    public FakedAutoSendWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NotNull
    @Override
    public Result doWork() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Timber.i(e);
        }
        return Result.success();
    }
}
