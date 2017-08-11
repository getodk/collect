/*
 * Copyright (C) 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * Activity to upload completed forms to gme.
 *
 * @author Carl Hartung (chartung@nafundi.com)
 */

package org.odk.collect.android.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.InstanceGoogleSheetsUploader;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

import static org.odk.collect.android.tasks.InstanceGoogleSheetsUploader.REQUEST_ACCOUNT_PICKER;
import static org.odk.collect.android.tasks.InstanceGoogleSheetsUploader.REQUEST_AUTHORIZATION;
import static org.odk.collect.android.tasks.InstanceGoogleSheetsUploader.REQUEST_PERMISSION_GET_ACCOUNTS;


public class GoogleSheetsUploaderActivity extends AppCompatActivity implements InstanceUploaderListener,
        EasyPermissions.PermissionCallbacks {
    private static final int PROGRESS_DIALOG = 1;
    private static final int GOOGLE_USER_DIALOG = 3;
    private static final String ALERT_MSG = "alertmsg";
    private static final String ALERT_SHOWING = "alertshowing";
    private GoogleAccountCredential credential;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private String alertMsg;
    private boolean alertShowing;
    private Long[] instancesToSend;
    private InstanceGoogleSheetsUploader instanceGoogleSheetsUploader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate: %s", ((savedInstanceState == null) ? "creating" : "re-initializing"));

        // if we start this activity, the following must be true:
        // 1) Google Sheets is selected in preferences
        // 2) A google user is selected

        // default initializers
        alertMsg = getString(R.string.please_wait);
        alertShowing = false;

        setTitle(getString(R.string.send_data));

        // get any simple saved state...
        // resets alert message and showing dialog if the screen is rotated
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ALERT_MSG)) {
                alertMsg = savedInstanceState.getString(ALERT_MSG);
            }
            if (savedInstanceState.containsKey(ALERT_SHOWING)) {
                alertShowing = savedInstanceState.getBoolean(ALERT_SHOWING, false);
            }
        }

        long[] selectedInstanceIDs;

        Intent intent = getIntent();
        selectedInstanceIDs = intent.getLongArrayExtra(FormEntryActivity.KEY_INSTANCES);

        instancesToSend = new Long[(selectedInstanceIDs == null) ? 0 : selectedInstanceIDs.length];
        if (selectedInstanceIDs != null) {
            for (int i = 0; i < selectedInstanceIDs.length; ++i) {
                instancesToSend[i] = selectedInstanceIDs[i];
            }
        }

        // at this point, we don't expect this to be empty...
        if (instancesToSend.length == 0) {
            Timber.e("onCreate: No instances to upload!");
            // drop through --
            // everything will process through OK
        } else {
            Timber.i("onCreate: Beginning upload of %d instances!", instancesToSend.length);
        }

        // Initialize credentials and service object.
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Collections.singleton(DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff());

        getResultsFromApi();
    }

    private void runTask() {
        instanceGoogleSheetsUploader = (InstanceGoogleSheetsUploader) getLastCustomNonConfigurationInstance();
        if (instanceGoogleSheetsUploader == null) {
            instanceGoogleSheetsUploader = new InstanceGoogleSheetsUploader(credential, GoogleSheetsUploaderActivity.this);

            // ensure we have a google account selected
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String googleUsername = prefs.getString(
                    PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT, null);
            if (googleUsername == null || googleUsername.equals("")) {
                showDialog(GOOGLE_USER_DIALOG);
                return;
            }

            showDialog(PROGRESS_DIALOG);

            instanceGoogleSheetsUploader.setUploaderListener(this);
            instanceGoogleSheetsUploader.execute(instancesToSend);
        } else {
            // it's not null, so we have a task running
            // progress dialog is handled by the system
        }
    }

    /*
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            ToastUtils.showShortToast("No network connection available.");
        } else {
            runTask();
        }
    }

    /*
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            // ensure we have a google account selected
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String googleUsername = prefs.getString(
                    PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT, "");
            if (!googleUsername.equals("")) {
                credential.setSelectedAccountName(googleUsername);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        credential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.request_permissions_google_account),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }


    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences prefs =
                                PreferenceManager.getDefaultSharedPreferences(this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT, accountName);
                        editor.apply();
                        credential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                dismissDialog(PROGRESS_DIALOG);
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                } else {
                    Timber.d("AUTHORIZE_DRIVE_ACCESS failed, asking to choose new account:");
                    finish();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onResume() {
        if (instanceGoogleSheetsUploader != null) {
            instanceGoogleSheetsUploader.setUploaderListener(this);
        }
        if (alertShowing) {
            createAlertDialog(alertMsg);
        }
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ALERT_MSG, alertMsg);
        outState.putBoolean(ALERT_SHOWING, alertShowing);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return instanceGoogleSheetsUploader;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (instanceGoogleSheetsUploader != null) {
            instanceGoogleSheetsUploader.setUploaderListener(null);
        }
        super.onDestroy();
    }

    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        try {
            dismissDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }

        if (result == null) {
            // probably got an auth request, so ignore
            return;
        }
        Timber.i("uploadingComplete: Processing results ( %d ) from upload of %d instances!",
                result.size(), instancesToSend.length);

        StringBuilder selection = new StringBuilder();
        Set<String> keys = result.keySet();
        StringBuilder message = new StringBuilder();

        if (keys.size() == 0) {
            if (instanceGoogleSheetsUploader.isAuthFailed()) {
                message.append(getString(R.string.google_auth_io_exception_msg));
                instanceGoogleSheetsUploader.setAuthFailed(false);
            } else {
                message.append(getString(R.string.no_forms_uploaded));
            }
        } else {
            Iterator<String> it = keys.iterator();

            String[] selectionArgs = new String[keys.size()];
            int i = 0;
            while (it.hasNext()) {
                String id = it.next();
                selection.append(InstanceColumns._ID + "=?");
                selectionArgs[i++] = id;
                if (i != keys.size()) {
                    selection.append(" or ");
                }
            }

            Cursor results = null;
            try {
                results = new InstancesDao().getInstancesCursor(selection.toString(), selectionArgs);
                if (results.getCount() > 0) {
                    results.moveToPosition(-1);
                    while (results.moveToNext()) {
                        String name = results.getString(results
                                .getColumnIndex(InstanceColumns.DISPLAY_NAME));
                        String id = results.getString(results.getColumnIndex(InstanceColumns._ID));
                        message.append(name).append(" - ").append(result.get(id)).append("\n\n");
                    }
                } else {
                    if (instanceGoogleSheetsUploader.isAuthFailed()) {
                        message.append(getString(R.string.google_auth_io_exception_msg));
                        instanceGoogleSheetsUploader.setAuthFailed(false);
                    } else {
                        message.append(getString(R.string.no_forms_uploaded));
                    }
                }
            } finally {
                if (results != null) {
                    results.close();
                }
            }
        }
        createAlertDialog(message.toString().trim());
    }

    @Override
    public void progressUpdate(int progress, int total) {
        alertMsg = getString(R.string.sending_items, String.valueOf(progress), String.valueOf(total));
        progressDialog.setMessage(alertMsg);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                Collect.getInstance().getActivityLogger()
                        .logAction(this, "onCreateDialog.PROGRESS_DIALOG", "show");

                progressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Collect.getInstance().getActivityLogger()
                                        .logAction(this, "onCreateDialog.PROGRESS_DIALOG",
                                                "cancel");
                                dialog.dismiss();
                                instanceGoogleSheetsUploader.cancel(true);
                                instanceGoogleSheetsUploader.setUploaderListener(null);
                                finish();
                            }
                        };
                progressDialog.setTitle(getString(R.string.uploading_data));
                progressDialog.setMessage(alertMsg);
                progressDialog.setIndeterminate(true);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return progressDialog;
            case GOOGLE_USER_DIALOG:
                AlertDialog.Builder gudBuilder = new AlertDialog.Builder(this);

                gudBuilder.setTitle(getString(R.string.no_google_account));
                gudBuilder.setMessage(getString(R.string.google_set_account));
                gudBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                gudBuilder.setCancelable(false);
                return gudBuilder.create();
        }
        return null;
    }

    private void createAlertDialog(String message) {
        Collect.getInstance().getActivityLogger().logAction(this, "createAlertDialog", "show");

        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.upload_results));
        alertDialog.setMessage(message);
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // ok
                        Collect.getInstance().getActivityLogger()
                                .logAction(this, "createAlertDialog", "OK");
                        // always exit this activity since it has no interface
                        alertShowing = false;
                        finish();
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(getString(R.string.ok), quitListener);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertShowing = true;
        alertMsg = message;
        alertDialog.show();
    }

    @Override
    public void authRequest(Uri url, HashMap<String, String> doneSoFar) {
        // in interface, but not needed
    }
}
