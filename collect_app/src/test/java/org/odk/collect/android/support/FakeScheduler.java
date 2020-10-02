package org.odk.collect.android.support;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.async.Cancellable;
import org.odk.collect.async.Scheduler;
import org.odk.collect.async.TaskSpec;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FakeScheduler implements Scheduler {

    private Runnable foregroundTask;
    private Runnable backgroundTask;
    private Boolean cancelled = false;
    private Boolean isRepeatRunning = false;

    @Override
    public <T> void immediate(Supplier<T> foreground, Consumer<T> background) {
        backgroundTask = () -> background.accept(foreground.get());
    }

    @Override
    public void immediate(@NotNull Runnable foreground) {
        foregroundTask = foreground;
    }

    @Override
    public void networkDeferred(@NotNull String tag, @NotNull TaskSpec spec) {

    }

    @Override
    public void networkDeferred(@NotNull String tag, @NotNull TaskSpec taskSpec, long repeatPeriod) {

    }

    @Override
    public Cancellable repeat(Runnable foreground, long repeatPeriod) {
        this.foregroundTask = foreground;
        isRepeatRunning = true;
        return () -> {
            isRepeatRunning = false;
            cancelled = true;
            return true;
        };
    }

    public void runForeground() {
        if (foregroundTask != null) {
            foregroundTask.run();
        }

    }

    public void runBackground() {
        if (backgroundTask != null) {
            backgroundTask.run();
        }
    }

    public Boolean hasBeenCancelled() {
        return cancelled;
    }

    public Boolean checkRepeatRunning() {
        return isRepeatRunning;
    }

    @Override
    public boolean isRunning(@NotNull String tag) {
        return false;
    }

    @Override
    public void cancelDeferred(@NotNull String tag) {

    }
}
