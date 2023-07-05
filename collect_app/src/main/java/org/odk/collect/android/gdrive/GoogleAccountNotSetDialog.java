package org.odk.collect.android.gdrive;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import static org.odk.collect.android.utilities.DialogUtils.showDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class GoogleAccountNotSetDialog {

    private GoogleAccountNotSetDialog() {

    }

    public static void show(Activity activity) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(org.odk.collect.strings.R.string.missing_google_account_dialog_title)
                .setMessage(org.odk.collect.strings.R.string.missing_google_account_dialog_desc)
                .setOnCancelListener(dialog -> {
                    dialog.dismiss();
                    if (activity != null) {
                        activity.finish();
                    }
                })
                .setPositiveButton(activity.getString(org.odk.collect.strings.R.string.ok), (dialog, which) -> {
                    dialog.dismiss();
                    activity.finish();
                })
                .create();

        showDialog(alertDialog, activity);
    }
}
