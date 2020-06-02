package org.odk.collect.android.utilities;

import android.os.AsyncTask;
import android.os.Handler;

import org.odk.collect.utilities.Cancellable;
import org.odk.collect.utilities.Scheduler;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An implementation of {@link Scheduler} that uses {@link Handler} and {@link AsyncTask} to schedule work on the
 * scheduling thread.
 */
public class HandlerAndAsyncTaskScheduler implements Scheduler {

    @Override
    public <T> void scheduleInBackground(Supplier<T> task, Consumer<T> callback) {
        new SupplierConsumerAsyncTask<>(task, callback).execute();
    }

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

    private static class SupplierConsumerAsyncTask<T> extends AsyncTask<Void, Void, T> {

        private final Supplier<T> supplier;
        private final Consumer<T> consumer;

        SupplierConsumerAsyncTask(Supplier<T> supplier, Consumer<T> consumer) {
            this.supplier = supplier;
            this.consumer = consumer;
        }

        @Override
        protected T doInBackground(Void... voids) {
            return supplier.get();
        }

        @Override
        protected void onPostExecute(T result) {
            consumer.accept(result);
        }
    }
}
