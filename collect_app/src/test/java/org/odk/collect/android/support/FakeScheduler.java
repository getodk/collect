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

    @Override
    public <T> void immediate(Supplier<T> foreground, Consumer<T> background) {
        backgroundTask = () -> background.accept(foreground.get());
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
        return () -> {
            cancelled = true;
            return true;
        };
    }

    public void runForeground() {
        foregroundTask.run();
    }

    public void runBackground() {
        if (backgroundTask == null) {
            return;
        }

        backgroundTask.run();
    }

    public Boolean hasBeenCancelled() {
        return cancelled;
    }

    @Override
    public boolean isRunning(@NotNull String tag) {
        return false;
    }

    @Override
    public void cancelDeferred(@NotNull String tag) {

    }
}
