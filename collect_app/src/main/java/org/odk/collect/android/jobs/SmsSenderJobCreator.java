package org.odk.collect.android.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class SmsSenderJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
            switch (tag) {
                case SmsSenderJob.TAG:
                    return new SmsSenderJob();
                default:
                    return null;
            }
        }
}

