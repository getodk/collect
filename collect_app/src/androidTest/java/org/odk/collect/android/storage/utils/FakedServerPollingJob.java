package org.odk.collect.android.storage.utils;

import androidx.annotation.NonNull;

import com.evernote.android.job.Job;

import timber.log.Timber;

public class FakedServerPollingJob extends Job {
    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Timber.i(e);
        }
        return Result.SUCCESS;
    }
}
