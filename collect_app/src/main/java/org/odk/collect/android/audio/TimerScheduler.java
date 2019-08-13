package org.odk.collect.android.audio;

import java.util.Timer;
import java.util.TimerTask;

public class TimerScheduler implements Scheduler {

    private Timer timer;

    public TimerScheduler() {
        timer = new Timer();
    }

    @Override
    public void schedule(Runnable task, long period) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, 0, period);
    }

    @Override
    public void cancel() {
        timer.cancel();
        timer = new Timer();
    }
}
