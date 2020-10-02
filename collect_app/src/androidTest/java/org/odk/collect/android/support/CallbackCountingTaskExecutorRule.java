package org.odk.collect.android.support;

import androidx.arch.core.executor.testing.CountingTaskExecutorRule;

public class CallbackCountingTaskExecutorRule extends CountingTaskExecutorRule {

    private Runnable finishedCallback;

    public void setFinishedCallback(Runnable callback) {
        this.finishedCallback = callback;
    }

    @Override
    protected void onIdle() {
        if (finishedCallback != null) {
            finishedCallback.run();
        }
    }
}
