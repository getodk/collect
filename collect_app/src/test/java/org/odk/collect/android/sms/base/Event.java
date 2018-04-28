package org.odk.collect.android.sms.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.callback.JobManagerCallback;

/**
 * Simplifies the Job Callback to a single action.
 */
public class Event implements JobManagerCallback {
    public interface Result {
        void onComplete(Job job);
    }

    private Result result;

    public Event(Result result) {
        this.result = result;
    }

    @Override
    public void onJobAdded(@NonNull Job job) {

    }

    @Override
    public void onJobRun(@NonNull Job job, int i) {
        result.onComplete(job);
    }

    @Override
    public void onJobCancelled(@NonNull Job job, boolean b, @Nullable Throwable throwable) {

    }

    @Override
    public void onDone(@NonNull Job job) {

    }

    @Override
    public void onAfterJobRun(@NonNull Job job, int i) {

    }
}
