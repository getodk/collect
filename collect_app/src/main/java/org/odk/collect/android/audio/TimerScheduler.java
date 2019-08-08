package org.odk.collect.android.audio;

import java.util.Timer;
import java.util.TimerTask;

public class TimerScheduler implements Scheduler {

    @Override
    public void schedule(Runnable task, long period) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, 0, period);
    }
}
