package org.odk.collect.android.utilities;

import android.os.Handler;

import org.odk.collect.utilities.Cancellable;
import org.odk.collect.utilities.Scheduler;

/**
 * An implementation of {@link Scheduler} that uses {@link Handler} to schedule work on the
 * scheduling thread.
 */
public class HandlerScheduler implements Scheduler {

    @Override
    public Cancellable schedule(Runnable task, long period) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                task.run();
                handler.postDelayed(this, period);
            }
        });

        return () -> {
            handler.removeCallbacksAndMessages(null);
            return true;
        };
    }
}
