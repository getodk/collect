package org.odk.collect.android.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.activities.FormChooserList;
import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.InstanceChooserList;
import org.odk.collect.android.activities.InstanceUploaderActivity;
import org.odk.collect.android.activities.InstanceUploaderList;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.listeners.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * PermissionUtils allows all permission related messages and checks to be encapsulated in one
 * area so that classes don't have to deal with this responsibility; they just receive a callback
 * that tells them if they have been granted the permission they requested.
 */

public class PermissionUtils {

    private PermissionUtils() {

    }

    /**
     * Checks to see if the user granted Collect the permissions necessary for reading
     * and writing to storage and if not utilizes the permissions API to request them.
     *
     * @param activity required for context and spawning of Dexter's activity that handles
     *                 permission checking.
     * @param action   is a listener that provides the calling component with the permission result.
     */
    public static void requestStoragePermissions(@NonNull Activity activity, @NonNull PermissionListener action) {

        MultiplePermissionsListener multiplePermissionsListener = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    action.granted();
                } else {
                    showAdditionalExplanation(activity, R.string.storage_runtime_permission_denied_title,
                            R.string.storage_runtime_permission_denied_desc, R.drawable.sd, action);
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
                .withErrorListener(error -> {
                    Timber.i(error.name());
                })
                .check();
    }

    public static void requestCameraPermission(@NonNull Activity activity, @NonNull PermissionListener action) {
        com.karumi.dexter.listener.single.PermissionListener permissionListener = new com.karumi.dexter.listener.single.PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                action.granted();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                showAdditionalExplanation(activity, R.string.camera_runtime_permission_denied_title,
                        R.string.camera_runtime_permission_denied_desc, R.drawable.ic_photo_camera, action);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };

        Dexter.withActivity(activity)
                .withPermission(
                        Manifest.permission.CAMERA
                ).withListener(permissionListener)
                .withErrorListener(error -> Timber.i(error.name()))
                .check();
    }
  
    public static void requestLocationPermissions(@NonNull Activity activity, @NonNull PermissionListener action) {
        MultiplePermissionsListener multiplePermissionsListener = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    action.granted();
                } else {
                    showAdditionalExplanation(activity, R.string.location_runtime_permissions_denied_title,
                            R.string.location_runtime_permissions_denied_desc, R.drawable.ic_place_black, action);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };

        Dexter.withActivity(activity)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ).withListener(multiplePermissionsListener)
                .withErrorListener(error -> Timber.i(error.name()))
                .check();
    }

    public static void requestRecordAudioPermission(@NonNull Activity activity, @NonNull PermissionListener action) {
        com.karumi.dexter.listener.single.PermissionListener permissionListener = new com.karumi.dexter.listener.single.PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                action.granted();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                showAdditionalExplanation(activity, R.string.record_audio_runtime_permission_denied_title,
                        R.string.record_audio_runtime_permission_denied_desc, R.drawable.ic_mic, action);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };

        Dexter.withActivity(activity)
                .withPermission(
                        Manifest.permission.RECORD_AUDIO
                ).withListener(permissionListener)
                .withErrorListener(error -> Timber.i(error.name()))
                .check();
    }

    public static void requestCameraAndRecordAudioPermissions(@NonNull Activity activity, @NonNull PermissionListener action) {
        MultiplePermissionsListener multiplePermissionsListener = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    action.granted();
                } else {
                    showAdditionalExplanation(activity, R.string.camera_runtime_permission_denied_title,
                            R.string.camera_runtime_permission_denied_desc, R.drawable.ic_photo_camera, action);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };

        Dexter.withActivity(activity)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                ).withListener(multiplePermissionsListener)
                .withErrorListener(error -> Timber.i(error.name()))
                .check();
    }

    public static void requestGetAccountsPermission(@NonNull Activity activity, @NonNull PermissionListener action) {
        com.karumi.dexter.listener.single.PermissionListener permissionListener = new com.karumi.dexter.listener.single.PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                action.granted();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                showAdditionalExplanation(activity, R.string.get_accounts_runtime_permission_denied_title,
                        R.string.get_accounts_runtime_permission_denied_desc, R.drawable.ic_get_accounts, action);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };

        Dexter.withActivity(activity)
                .withPermission(
                        Manifest.permission.GET_ACCOUNTS
                ).withListener(permissionListener)
                .withErrorListener(error -> Timber.i(error.name()))
                .check();
    }

    public static void requestSendSMSPermission(@NonNull Activity activity, @NonNull PermissionListener action) {
        com.karumi.dexter.listener.single.PermissionListener permissionListener = new com.karumi.dexter.listener.single.PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                action.granted();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                showAdditionalExplanation(activity, R.string.send_sms_runtime_permission_denied_title,
                        R.string.send_sms_runtime_permission_denied_desc, R.drawable.ic_sms, action);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };

        Dexter.withActivity(activity)
                .withPermission(
                        Manifest.permission.SEND_SMS
                ).withListener(permissionListener)
                .withErrorListener(error -> Timber.i(error.name()))
                .check();
    }

    public static void requestReadPhoneStatePermission(@NonNull Activity activity, @NonNull PermissionListener action, boolean displayPermissionDeniedDialog) {
        com.karumi.dexter.listener.single.PermissionListener permissionListener = new com.karumi.dexter.listener.single.PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                action.granted();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                if (displayPermissionDeniedDialog) {
                    showAdditionalExplanation(activity, R.string.read_phone_state_runtime_permission_denied_title,
                            R.string.read_phone_state_runtime_permission_denied_desc, R.drawable.ic_phone, action);
                } else {
                    action.denied();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };

        Dexter.withActivity(activity)
                .withPermission(
                        Manifest.permission.READ_PHONE_STATE
                ).withListener(permissionListener)
                .withErrorListener(error -> Timber.i(error.name()))
                .check();
    }

    public static boolean checkIfStoragePermissionsGranted(Context context) {
        int read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        int write = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkIfCameraPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkIfLocationPermissionsGranted(Context context) {
        int accessFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int accessCoarseLocation = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        return accessFineLocation == PackageManager.PERMISSION_GRANTED
                && accessCoarseLocation == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkIfCameraAndRecordAudioPermissionsGranted(Context context) {
        int cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        int recordAudioPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);

        return cameraPermission == PackageManager.PERMISSION_GRANTED
                && recordAudioPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkIfGetAccountsPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkIfReadPhoneStatePermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks to see if an activity is one of the entry points to the app i.e
     * an activity that has a view action that can launch the app.
     *
     * @param activity that has permission requesting code.
     * @return true if the activity is an entry point to the app.
     */
    public static boolean isEntryPointActivity(CollectAbstractActivity activity) {

        List<Class<?>> activities = new ArrayList<>();
        activities.add(FormEntryActivity.class);
        activities.add(InstanceChooserList.class);
        activities.add(FormChooserList.class);
        activities.add(InstanceUploaderList.class);
        activities.add(SplashScreenActivity.class);
        activities.add(FormDownloadList.class);
        activities.add(InstanceUploaderActivity.class);

        for (Class<?> act : activities) {
            if (activity.getClass().equals(act)) {
                return true;
            }
        }

        return false;
    }

    public static void finishAllActivities(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.finishAndRemoveTask();
        } else {
            activity.finishAffinity();
        }
    }

    private static void showAdditionalExplanation(@NonNull Activity activity, int title,
                                                  int message, int drawable,
                                                  @NonNull PermissionListener action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_Light_Dialog);

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> action.denied())
                .setCancelable(false)
                .setIcon(drawable);

        DialogUtils.showDialog(builder.create(), activity);
    }
}
