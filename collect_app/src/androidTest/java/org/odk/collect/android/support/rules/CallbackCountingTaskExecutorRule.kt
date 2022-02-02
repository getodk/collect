package org.odk.collect.android.support.rules

import androidx.arch.core.executor.testing.CountingTaskExecutorRule

class CallbackCountingTaskExecutorRule : CountingTaskExecutorRule() {

    private var finishedCallback: Runnable? = null

    fun setFinishedCallback(callback: Runnable?) {
        finishedCallback = callback
    }

    override fun onIdle() {
        finishedCallback?.run()
    }
}
