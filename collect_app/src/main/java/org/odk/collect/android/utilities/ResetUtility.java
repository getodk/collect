package org.odk.collect.android.utilities;

import android.content.Context;
import android.preference.PreferenceManager;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.odk.collect.android.listeners.DeleteInstancesListener;
import org.odk.collect.android.provider.DatabaseReader;
import org.odk.collect.android.tasks.DeleteInstancesTask;

public class ResetUtility {

    public void reset(final Context context, boolean resetPreferences, boolean resetInstances,
            final ResetResultCallback callback) {

        Deferred deferred = new DeferredObject<Void, String, Void>();
        Promise promise = deferred.promise();

        if (resetPreferences) {
            promise = promise
                    .then(new DonePipe() {
                        @Override
                        public Deferred pipeDone(Object result) {
                            resetPreferences(context);
                            return new DeferredObject().resolve(null);
                        }
                    });
        }

        if (resetInstances) {
            promise = promise.then(new DonePipe() {
                @Override
                public Promise pipeDone(Object result) {
                    DeferredObject dobj = new DeferredObject<>();
                    resetInstances(context, dobj);

                    return dobj;
                }
            });
        }

        promise
                .done(new DoneCallback() {
                    @Override
                    public void onDone(Object result) {
                        callback.doneResetting();
                    }
                })
                .fail(new FailCallback<String>() {
                    @Override
                    public void onFail(String errorMessage) {
                        callback.failedToReset(errorMessage);
                    }
                });

        deferred.resolve(null);
    }

    private void resetInstances(Context context, final DeferredObject deferred) {
        final Long[] allInstances = new DatabaseReader().getAllInstancesIds(context);

        DeleteInstancesTask task = new DeleteInstancesTask();
        task.setContentResolver(context.getContentResolver());
        task.setDeleteListener(new DeleteInstancesListener() {
            @Override
            public void deleteComplete(int deletedInstances) {
                if (deletedInstances == allInstances.length) {
                    deferred.resolve(null);
                } else {
                    deferred.reject(
                            String.format("We've been able to delete only %d instances out of %d",
                                    deletedInstances, allInstances.length));
                }
            }
        });

        task.execute(allInstances);
    }

    private void resetPreferences(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .clear()
                .commit();
    }
}
