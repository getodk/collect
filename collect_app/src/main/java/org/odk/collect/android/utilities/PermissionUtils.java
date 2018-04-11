package org.odk.collect.android.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.PermissionListener;

import java.util.List;

import timber.log.Timber;

/**
 * PermissionUtils allows all permission related messages and checks to be encapsulated in one
 * area so that classes don't have to deal with this responsibility; they just receive a callback
 * that tells them if they have been granted the permission they requested.
 */

public class PermissionUtils {

    /**
     * Checks to see if the user granted Collect the permissions necessary for reading
     * and writing to storage and if not utilizes the permissions API to request them.
     *
     * @param activity
     * @param action
     */
    public static void requestStoragePermissions(Activity activity, PermissionListener action) {

        if (activity == null) {
            throw new NullPointerException("Activity can't be null.");
        }

        if (action == null) {
            throw new NullPointerException("Action listener can't be null. The calling activity needs to react to these operations.");
        }

        MultiplePermissionsListener multiplePermissionsListener = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    action.granted();
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_Light_Dialog);

                    builder.setTitle(R.string.storage_runtime_permission_denied_title)
                            .setMessage(R.string.storage_runtime_permission_denied_desc)
                            .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                                action.denied();
                            })
                            .setIcon(R.drawable.sd)
                            .show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };

        Dexter.withActivity(activity)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(multiplePermissionsListener)
                .withErrorListener(error -> Timber.i(error.name()))
                .check();
    }

    public static boolean checkIfStoragePermissionsGranted(Context context) {
        int read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        int write = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
}
