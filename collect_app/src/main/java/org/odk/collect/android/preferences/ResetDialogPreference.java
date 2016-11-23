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

    private CheckBox mPreferences;
    private CheckBox mInstances;
    private CheckBox mForms;
    private CheckBox mLayers;
    private CheckBox mMetaData;
    private CheckBox mCache;
    private Context mContext;
    private ProgressDialog mProgressDialog;

    public ResetDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.reset_dialog_layout);
        mContext = context;
    }

    @Override
    public void onBindDialogView(View view) {
        mPreferences = (CheckBox) view.findViewById(R.id.preferences);
        mInstances = (CheckBox) view.findViewById(R.id.instances);
        mForms = (CheckBox) view.findViewById(R.id.forms);
        mLayers = (CheckBox) view.findViewById(R.id.layers);
        mMetaData = (CheckBox) view.findViewById(R.id.metadata);
        mCache = (CheckBox) view.findViewById(R.id.cache);

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
        final boolean resetForms = mForms.isChecked();
        final boolean resetLayers = mLayers.isChecked();
        final boolean resetMetaData = mMetaData.isChecked();
        final boolean resetCache = mCache.isChecked();

        if (!resetPreferences
                && !resetInstances
                && !resetForms
                && !resetLayers
                && !resetMetaData
                && !resetCache) {
            Toast.makeText(getContext(), R.string.reset_dialog_nothing, Toast.LENGTH_LONG).show();
            return;
        }

        showProgressDialog();

        new ResetUtility().reset(getContext(),
                resetPreferences,
                resetInstances,
                resetForms,
                resetLayers,
                resetMetaData,
                resetCache,
                this);
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