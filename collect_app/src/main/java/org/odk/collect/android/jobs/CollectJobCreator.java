package org.odk.collect.android.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import org.odk.collect.android.tasks.ServerPollingJob;

public class CollectJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case SmsSenderJob.TAG:
                return new SmsSenderJob();

            case ServerPollingJob.TAG:
                return new ServerPollingJob();

            default:
                return null;
        }
    }
}

