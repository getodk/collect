package org.odk.collect.android.support;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.work.WorkManager;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.async.Cancellable;
import org.odk.collect.async.CoroutineAndWorkManagerScheduler;
import org.odk.collect.async.Scheduler;
import org.odk.collect.async.TaskSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TestScheduler implements Scheduler {

    private final Scheduler wrappedScheduler;

    private final Object lock = new Object();
    private int tasks;
    private Runnable finishedCallback;

    private final List<DeferredTask> deferredTasks = new ArrayList<>();

    public TestScheduler() {
        WorkManager workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext());
        this.wrappedScheduler = new CoroutineAndWorkManagerScheduler(workManager);
    }

    @Override
    public Cancellable repeat(@NotNull Runnable foreground, long repeatPeriod) {
        increment();

        return wrappedScheduler.repeat(() -> {
            foreground.run();
            decrement();
        }, repeatPeriod);
    }

    @Override
    public <T> void immediate(@NotNull Supplier<T> foreground, @NotNull Consumer<T> background) {
        increment();

        wrappedScheduler.immediate(foreground, t -> {
            background.accept(t);
            decrement();
        });
    }

    @Override
    public void immediate(@NotNull Runnable foreground) {
        increment();

        wrappedScheduler.immediate(() -> {
            foreground.run();
            decrement();
        });
    }

    @Override
    public void networkDeferred(@NotNull String tag, @NotNull TaskSpec spec, @NotNull Map<String, String> inputData) {
        deferredTasks.add(new DeferredTask(tag, spec, null, inputData));
    }

    @Override
    public void networkDeferred(@NotNull String tag, @NotNull TaskSpec spec, long repeatPeriod, @NotNull Map<String, String> inputData) {
        cancelDeferred(tag);
        deferredTasks.add(new DeferredTask(tag, spec, repeatPeriod, inputData));
    }

    @Override
    public void cancelDeferred(@NotNull String tag) {
        deferredTasks.removeIf(t -> t.getTag().equals(tag));
    }

    @Override
    public boolean isDeferredRunning(@NotNull String tag) {
        return wrappedScheduler.isDeferredRunning(tag);
    }

    public void runDeferredTasks() {
        Context applicationContext = ApplicationProvider.getApplicationContext();

        for (DeferredTask deferredTask : deferredTasks) {
            deferredTask.getSpec().getTask(applicationContext, deferredTask.getInputData()).get();
        }

        // Remove non repeating tasks
        deferredTasks.removeIf(deferredTask -> deferredTask.repeatPeriod == null);
    }

    public void setFinishedCallback(Runnable callback) {
        this.finishedCallback = callback;
    }

    private void increment() {
        synchronized (lock) {
            tasks++;
        }
    }

    private void decrement() {
        synchronized (lock) {
            tasks--;

            if (tasks == 0 && finishedCallback != null) {
                finishedCallback.run();
            }
        }
    }

    public int getTaskCount() {
        synchronized (lock) {
            return tasks;
        }
    }

    public List<DeferredTask> getDeferredTasks() {
        return deferredTasks;
    }

    @Override
    public void cancelAllDeferred() {
    }

    public static class DeferredTask {

        private final String tag;
        private final TaskSpec spec;
        private final Long repeatPeriod;
        private final Map<String, String> inputData;

        public DeferredTask(String tag, TaskSpec spec, Long repeatPeriod, Map<String, String> inputData) {
            this.tag = tag;
            this.spec = spec;
            this.repeatPeriod = repeatPeriod;
            this.inputData = inputData;
        }

        public String getTag() {
            return tag;
        }

        public TaskSpec getSpec() {
            return spec;
        }

        public long getRepeatPeriod() {
            return repeatPeriod;
        }

        public Map<String, String> getInputData() {
            return inputData;
        }
    }
}
