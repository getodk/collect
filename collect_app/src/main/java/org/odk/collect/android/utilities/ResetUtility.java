package org.odk.collect.android.utilities;

import android.content.Context;
import android.preference.PreferenceManager;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.DeleteFormsListener;
import org.odk.collect.android.listeners.DeleteInstancesListener;
import org.odk.collect.android.provider.DatabaseReader;
import org.odk.collect.android.tasks.DeleteFormsTask;
import org.odk.collect.android.tasks.DeleteInstancesTask;

import java.io.File;

public class ResetUtility {

    public void reset(final Context context, boolean resetPreferences, boolean resetInstances,
            boolean resetForms, boolean resetLayers, final ResetResultCallback callback) {

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
                    DeferredObject def = new DeferredObject<>();
                    resetInstances(context, def);

                    return def;
                }
            });
        }

        if (resetForms) {
            promise = promise.then(new DonePipe() {
                @Override
                public Promise pipeDone(Object result) {
                    DeferredObject def = new DeferredObject<>();
                    resetForms(context, def);

                    return def;
                }
            });
        }

        if (resetLayers) {
            promise = promise.then(new DonePipe() {
                @Override
                public Promise pipeDone(Object result) {
                    DeferredObject def = new DeferredObject<>();
                    resetLayers(def);

                    return def;
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

    private void resetLayers(DeferredObject def) {
        File[] files = new File(Collect.OFFLINE_LAYERS).listFiles();

        for (File f : files) {
            DeletionResult result = deleteRecursive(f);
            if (result.isSuccessful() == false) {
                def.reject(String.format("Could not delete file %s", result.getPath()));
            }
        }

        def.resolve(null);
    }

    private DeletionResult deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                DeletionResult result = deleteRecursive(child);
                if (result.isSuccessful() == false) {
                    return result;
                }
            }
        }

        return new DeletionResult(fileOrDirectory.delete(), fileOrDirectory.getPath());
    }

    private void resetForms(Context context, final DeferredObject def) {
        final Long[] allForms = new DatabaseReader().getAllFormsIDs(context);

        DeleteFormsTask task = new DeleteFormsTask();
        task.setContentResolver(context.getContentResolver());
        task.setDeleteListener(new DeleteFormsListener() {
            @Override
            public void deleteComplete(int deletedForms) {
                if (deletedForms == allForms.length) {
                    def.resolve(null);
                } else {
                    def.reject(
                            String.format("We've been able to delete only %d blank forms out of %d",
                                    deletedForms, allForms.length));
                }
            }
        });

        task.execute(allForms);
    }

    private void resetInstances(Context context, final DeferredObject deferred) {
        final Long[] allInstances = new DatabaseReader().getAllInstancesIDs(context);

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

    private class DeletionResult {
        private boolean mIsSuccessful;
        private String mPath;

        public DeletionResult(boolean isSuccessful, String path) {
            mIsSuccessful = isSuccessful;
            mPath = path;
        }

        public String getPath() {
            return mPath;
        }

        public boolean isSuccessful() {
            return mIsSuccessful;
        }
    }
}
