package org.odk.collect.android.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ResetResultCallback;
import org.odk.collect.android.utilities.ResetUtility;

public class ResetDialogPreference extends DialogPreference implements
        DialogInterface.OnClickListener, ResetResultCallback {
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
    public void onBindDialogView(View view) {
        mPreferences = (CheckBox) view.findViewById(R.id.preferences);
        mInstances = (CheckBox) view.findViewById(R.id.instances);

        super.onBindDialogView(view);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            resetSelected();
        }
    }

    @Override
    public void doneResetting() {
        hideProgressDialog();
        showSuccessMessage();
        ((Activity) mContext).finish();
    }

    @Override
    public void failedToReset(String errorMessage) {
        hideProgressDialog();
        showErrorMessage(errorMessage);
    }

    private void resetSelected() {
        final boolean resetPreferences = mPreferences.isChecked();
        final boolean resetInstances = mInstances.isChecked();

        if (!resetPreferences && !resetInstances) {
            Toast.makeText(getContext(), R.string.reset_dialog_nothing, Toast.LENGTH_LONG).show();
            return;
        }

        showProgressDialog();

        new ResetUtility().reset(getContext(), resetPreferences, resetInstances, this);
    }

    private void showProgressDialog() {
        mProgressDialog = ProgressDialog.show(getContext(), "Please wait ...", "Resetting...",
                true);
    }

    private void hideProgressDialog() {
        mProgressDialog.dismiss();
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
}