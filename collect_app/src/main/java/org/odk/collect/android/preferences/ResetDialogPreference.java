package org.odk.collect.android.preferences;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.DeleteInstancesListener;
import org.odk.collect.android.provider.DatabaseReader;
import org.odk.collect.android.tasks.DeleteInstancesTask;

public class ResetDialogPreference extends DialogPreference implements
        DialogInterface.OnClickListener {
    final static String TAG = "ResetDialogPreference";


    CheckBox mPreferences;
    CheckBox mInstances;
    Context mContext;
    ProgressDialog mProgressDialog;

    public ResetDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.reset_dialog_layout);
        mContext = context;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            resetSelected();
        }
    }

    private void resetSelected() {
        final boolean resetPreferences = mPreferences.isChecked();
        final boolean resetInstances = mInstances.isChecked();

        if (!resetPreferences && !resetInstances) {
            Toast.makeText(getContext(), R.string.reset_dialog_nothing, Toast.LENGTH_LONG).show();
            return;
        }

        showProgressDialog();

        Deferred deferred = new DeferredObject<Void, String, Void>();
        Promise promise = deferred.promise();

        if (resetPreferences) {
            promise = promise
                    .then(new DonePipe() {
                        @Override
                        public Deferred pipeDone(Object result) {
                            resetPreferences();
                            return new DeferredObject().resolve(null);
                        }
                    });
        }

        if (resetInstances) {
            promise = promise.then(new DonePipe() {
                @Override
                public Promise pipeDone(Object result) {
                    DeferredObject dobj = new DeferredObject<>();
                    resetInstances(dobj);

                    return dobj;
                }
            });
        }

        promise
                .done(new DoneCallback() {
                    @Override
                    public void onDone(Object result) {
                        hideProgressDialog();
                        showSuccessMessage();
                        ((Activity) mContext).finish();
                    }
                })
                .fail(new FailCallback<String>() {
                    @Override
                    public void onFail(String errorMessage) {
                        hideProgressDialog();
                        showErrorMessage(errorMessage);
                    }
                });

        deferred.resolve(null);
    }

    private void hideProgressDialog() {
        mProgressDialog.dismiss();
    }

    private void showProgressDialog() {
        mProgressDialog = ProgressDialog.show(getContext(), "Please wait ...", "Resetting...",
                true);
    }

    private void showSuccessMessage() {
        Toast.makeText(getContext(), R.string.reset_dialog_finished,
                Toast.LENGTH_LONG).show();
    }

    private void showErrorMessage(String errorMessage) {
        new AlertDialog.Builder(getContext())
                .setTitle("Error Occurred")
                .setMessage(errorMessage)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void resetInstances(final DeferredObject deferred) {
        final Long[] allInstances = new DatabaseReader().getAllInstancesIds(getContext());

        DeleteInstancesTask task = new DeleteInstancesTask();
        task.setContentResolver(getContext().getContentResolver());
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

    private void resetPreferences() {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .clear()
                .commit();
    }

    @Override
    public void onBindDialogView(View view) {
        mPreferences = (CheckBox) view.findViewById(R.id.preferences);
        mInstances = (CheckBox) view.findViewById(R.id.instances);

        super.onBindDialogView(view);
    }
}