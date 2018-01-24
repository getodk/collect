package org.odk.collect.android.location.activity;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerActivity;
import org.odk.collect.android.location.GeoActivity;

import javax.inject.Inject;

@PerActivity
public class ShowGpsDisabledAlert {

    @Nullable
    private AlertDialog alertDialog;

    @NonNull
    private final Activity activity;

    @Inject
    ShowGpsDisabledAlert(@NonNull GeoActivity activity) {
        this.activity = activity;
    }

    public void show(@SuppressWarnings("unused") Object __) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setMessage(activity.getString(R.string.gps_enable_message))
                .setCancelable(false)
                .setPositiveButton(activity.getString(R.string.enable_gps),
                        (dialog, id) -> activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0));

        alertDialogBuilder.setNegativeButton(activity.getString(R.string.cancel),
                (dialog, id) -> {
                    dialog.cancel();
                    alertDialog = null;
                });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
