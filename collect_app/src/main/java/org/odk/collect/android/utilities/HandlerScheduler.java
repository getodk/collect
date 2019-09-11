package org.odk.collect.android.utilities;

import android.os.Handler;

/**
 * An implementation of {@link Scheduler} that uses {@link Handler} to schedule work on the
 * scheduling thread.
 */
public class HandlerScheduler implements Scheduler {

    private final Handler handler;

    public HandlerScheduler() {
        handler = new Handler();
    }

    @Override
    public void schedule(Runnable task, long period) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                task.run();
                handler.postDelayed(this, period);
            }
        });
    }

    @Override
    public void cancel() {
        handler.removeCallbacksAndMessages(null);
    }
}
