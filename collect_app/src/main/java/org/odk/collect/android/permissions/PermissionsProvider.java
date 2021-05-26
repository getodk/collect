package org.odk.collect.android.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.DialogUtils;

import java.util.List;

import timber.log.Timber;

/**
 * PermissionsProvider allows all permission related messages and checks to be encapsulated in one
 * area so that classes don't have to deal with this responsibility; they just receive a callback
 * that tells them if they have been granted the permission they requested.
 */
public class PermissionsProvider {
    private final PermissionsChecker permissionsChecker;

    public PermissionsProvider(PermissionsChecker permissionsChecker) {
        this.permissionsChecker = permissionsChecker;
    }

    public boolean isCameraPermissionGranted() {
        return permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA);
    }

    public boolean areLocationPermissionsGranted() {
        return permissionsChecker.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public boolean areCameraAndRecordAudioPermissionsGranted() {
        return permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);
    }

    public boolean isGetAccountsPermissionGranted() {
        return permissionsChecker.isPermissionGranted(Manifest.permission.GET_ACCOUNTS);
    }

    public boolean isReadPhoneStatePermissionGranted() {
        return permissionsChecker.isPermissionGranted(Manifest.permission.READ_PHONE_STATE);
    }

    public void requestReadStoragePermission(Activity activity, @NonNull PermissionListener action) {
        requestPermissions(activity, new PermissionListener() {
            @Override
            public void granted() {
                action.granted();
            }

            @Override
            public void denied() {
                showAdditionalExplanation(activity, R.string.storage_runtime_permission_denied_title,
                        R.string.storage_runtime_permission_denied_desc, R.drawable.sd, action);
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public void requestCameraPermission(Activity activity, @NonNull PermissionListener action) {
        requestPermissions(activity, new PermissionListener() {
            @Override
            public void granted() {
                action.granted();
            }

            @Override
            public void denied() {
                showAdditionalExplanation(activity, R.string.camera_runtime_permission_denied_title,
                        R.string.camera_runtime_permission_denied_desc, R.drawable.ic_photo_camera, action);
            }
        }, Manifest.permission.CAMERA);
    }

    public void requestLocationPermissions(Activity activity, @NonNull PermissionListener action) {
        requestPermissions(activity, new PermissionListener() {
            @Override
            public void granted() {
                action.granted();
            }

            @Override
            public void denied() {
                showAdditionalExplanation(activity, R.string.location_runtime_permissions_denied_title,
                        R.string.location_runtime_permissions_denied_desc, R.drawable.ic_room_black_24dp, action);
            }
        }, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public void requestRecordAudioPermission(Activity activity, @NonNull PermissionListener action) {
        requestPermissions(activity, new PermissionListener() {
            @Override
            public void granted() {
                action.granted();
            }

            @Override
            public void denied() {
                showAdditionalExplanation(activity, R.string.record_audio_runtime_permission_denied_title,
                        R.string.record_audio_runtime_permission_denied_desc, R.drawable.ic_mic, action);
            }
        }, Manifest.permission.RECORD_AUDIO);
    }

    public void requestCameraAndRecordAudioPermissions(Activity activity, @NonNull PermissionListener action) {
        requestPermissions(activity, new PermissionListener() {
            @Override
            public void granted() {
                action.granted();
            }

            @Override
            public void denied() {
                showAdditionalExplanation(activity, R.string.camera_runtime_permission_denied_title,
                        R.string.camera_runtime_permission_denied_desc, R.drawable.ic_photo_camera, action);
            }
        }, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);
    }

    public void requestGetAccountsPermission(Activity activity, @NonNull PermissionListener action) {
        requestPermissions(activity, new PermissionListener() {
            @Override
            public void granted() {
                action.granted();
            }

            @Override
            public void denied() {
                showAdditionalExplanation(activity, R.string.get_accounts_runtime_permission_denied_title,
                        R.string.get_accounts_runtime_permission_denied_desc, R.drawable.ic_get_accounts, action);
            }
        }, Manifest.permission.GET_ACCOUNTS);
    }

    public void requestReadPhoneStatePermission(Activity activity, boolean displayPermissionDeniedDialog, @NonNull PermissionListener action) {
        requestPermissions(activity, new PermissionListener() {
            @Override
            public void granted() {
                action.granted();
            }

            @Override
            public void denied() {
                if (displayPermissionDeniedDialog) {
                    showAdditionalExplanation(activity, R.string.read_phone_state_runtime_permission_denied_title,
                            R.string.read_phone_state_runtime_permission_denied_desc, R.drawable.ic_phone, action);
                } else {
                    action.denied();
                }
            }
        }, Manifest.permission.READ_PHONE_STATE);
    }

    protected void requestPermissions(Activity activity, @NonNull PermissionListener listener, String... permissions) {
        DexterBuilder builder = null;

        if (permissions.length == 1) {
            builder = createSinglePermissionRequest(activity, permissions[0], listener);
        } else if (permissions.length > 1) {
            builder = createMultiplePermissionsRequest(activity, listener, permissions);
        }

        if (builder != null) {
            builder.withErrorListener(error -> Timber.i(error.name())).check();
        }
    }

    private DexterBuilder createSinglePermissionRequest(Activity activity, String permission, PermissionListener listener) {
        return Dexter.withContext(activity)
                .withPermission(permission)
                .withListener(new com.karumi.dexter.listener.single.PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        listener.granted();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        listener.denied();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                });
    }

    private DexterBuilder createMultiplePermissionsRequest(Activity activity, PermissionListener listener, String[] permissions) {
        return Dexter.withContext(activity)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            listener.granted();
                        } else {
                            listener.denied();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                });
    }

    protected void showAdditionalExplanation(Activity activity, int title, int message, int drawable, @NonNull PermissionListener action) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> action.denied())
                .setCancelable(false)
                .setIcon(drawable)
                .create();

        DialogUtils.showDialog(alertDialog, activity);
    }

    public void requestReadUriPermission(Activity activity, Uri uri, ContentResolver contentResolver, PermissionListener listener) {
        try (Cursor ignored = contentResolver.query(uri, null, null, null, null)) {
            listener.granted();
        } catch (SecurityException e) {
            requestReadStoragePermission(activity, new PermissionListener() {
                @Override
                public void granted() {
                    listener.granted();
                }

                @Override
                public void denied() {
                    listener.denied();
                }
            });
        } catch (Exception | Error e) {
            listener.denied();
        }
    }
}