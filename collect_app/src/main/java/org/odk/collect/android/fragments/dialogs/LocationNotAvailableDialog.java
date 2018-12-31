package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import org.odk.collect.android.R;

import timber.log.Timber;

public class LocationNotAvailableDialog extends DialogFragment {

    public static final String LOCATION_NOT_AVAILABLE_DIALOG_TAG = "locationNotAvailableDialogTag";

    public static LocationNotAvailableDialog newInstance() {
        return new LocationNotAvailableDialog();
    }

    /*
    We keep this just in case to avoid problems if someone tries to show a dialog after
    the activity’s state have been saved. Basically it shouldn't take place since we should control
    the activity state if we want to show a dialog (especially after long tasks).
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            manager
                    .beginTransaction()
                    .add(this, tag)
                    .commit();
        } catch (IllegalStateException e) {
            Timber.w(e);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_place_black)
                .setTitle(R.string.provider_disabled_error)
                .setMessage(R.string.location_not_available_dialog_message)
                .setPositiveButton(R.string.go_to_settings, (dialog, id) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .create();
    }
}
