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
 * @author Carl Hartung (chartung@nafundi.com)
 */

package org.odk.collect.android.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.FileArrayAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.GoogleDriveFormDownloadListener;
import org.odk.collect.android.listeners.TaskListener;
import org.odk.collect.android.logic.DriveListItem;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

import static org.odk.collect.android.tasks.InstanceGoogleSheetsUploader.REQUEST_ACCOUNT_PICKER;
import static org.odk.collect.android.tasks.InstanceGoogleSheetsUploader.REQUEST_AUTHORIZATION;
import static org.odk.collect.android.tasks.InstanceGoogleSheetsUploader.REQUEST_PERMISSION_GET_ACCOUNTS;

public class GoogleDriveActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,
        TaskListener, GoogleDriveFormDownloadListener, EasyPermissions.PermissionCallbacks, AdapterView.OnItemClickListener {

    private static final int PROGRESS_DIALOG = 1;
    private static final int GOOGLE_USER_DIALOG = 3;
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 5555;
    private static final int COMPLETE_AUTHORIZATION_REQUEST_CODE = 4322;
    private static final String MY_DRIVE_KEY = "mydrive";
    private static final String PATH_KEY = "path";
    private static final String DRIVE_ITEMS_KEY = "drive_list";
    private static final String PARENT_KEY = "parent";
    private static final String ALERT_MSG_KEY = "alertmsg";
    private static final String ALERT_SHOWING_KEY = "alertshowing";
    private static final String ROOT_KEY = "root";
    private static final String FILE_LIST_KEY = "fileList";
    private static final String PARENT_ID_KEY = "parentId";
    private static final String CURRENT_ID_KEY = "currentDir";
    protected GoogleAccountCredential credential;
    private Button rootButton;
    private Button backButton;
    private Button downloadButton;
    private ImageButton searchButton;
    private EditText searchText;
    private Stack<String> currentPath = new Stack<>();
    private Stack<String> folderIdStack = new Stack<>();
    private String alertMsg;
    private boolean alertShowing;
    private String rootId = null;
    private boolean myDrive;
    private FileArrayAdapter adapter;
    private RetrieveDriveFileContentsAsyncTask retrieveDriveFileContentsAsyncTask;
    private GetFileTask getFileTask;
    private String parentId;
    private ArrayList<DriveListItem> toDownload;
    private Drive driveService;
    private ListView listView;
    private TextView emptyView;

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(getString(R.string.google_drive));
        setSupportActionBar(toolbar);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setProgressBarVisibility(true);
        setContentView(R.layout.drive_layout);
        listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);
        emptyView = (TextView) findViewById(android.R.id.empty);

        initToolbar();

        parentId = null;
        alertShowing = false;
        toDownload = new ArrayList<>();

        if (savedInstanceState != null && savedInstanceState.containsKey(MY_DRIVE_KEY)) {
            // recover state on rotate
            myDrive = savedInstanceState.getBoolean(MY_DRIVE_KEY);
            String[] patharray = savedInstanceState.getStringArray(PATH_KEY);
            currentPath = buildPath(patharray);

            parentId = savedInstanceState.getString(PARENT_KEY);
            alertMsg = savedInstanceState.getString(ALERT_MSG_KEY);
            alertShowing = savedInstanceState.getBoolean(ALERT_SHOWING_KEY);

            ArrayList<DriveListItem> dl = savedInstanceState
                    .getParcelableArrayList(DRIVE_ITEMS_KEY);
            adapter = new FileArrayAdapter(GoogleDriveActivity.this, R.layout.two_item_image, dl);
            listView.setAdapter(adapter);
            adapter.setEnabled(true);
        } else {
            // new
            myDrive = false;

            if (!isDeviceOnline()) {
                createAlertDialog(getString(R.string.no_connection));
            }
        }

        // restore any task state
        if (getLastCustomNonConfigurationInstance() instanceof RetrieveDriveFileContentsAsyncTask) {
            retrieveDriveFileContentsAsyncTask =
                    (RetrieveDriveFileContentsAsyncTask) getLastNonConfigurationInstance();
            setProgressBarIndeterminateVisibility(true);
        } else {
            getFileTask = (GetFileTask) getLastNonConfigurationInstance();
            if (getFileTask != null) {
                getFileTask.setGoogleDriveFormDownloadListener(this);
            }
        }
        if (getFileTask != null && getFileTask.getStatus() == AsyncTask.Status.FINISHED) {
            try {
                dismissDialog(PROGRESS_DIALOG);
            } catch (Exception e) {
                Timber.i("Exception was thrown while dismissing a dialog.");
            }
        }
        if (alertShowing) {
            try {
                dismissDialog(PROGRESS_DIALOG);
            } catch (Exception e) {
                // don't care...
                Timber.i("Exception was thrown while dismissing a dialog.");
            }
            createAlertDialog(alertMsg);
        }

        rootButton = (Button) findViewById(R.id.root_button);
        if (myDrive) {
            rootButton.setText(getString(R.string.go_shared));
        } else {
            rootButton.setText(getString(R.string.go_drive));
        }
        rootButton.setOnClickListener(this);

        backButton = (Button) findViewById(R.id.back_button);
        backButton.setEnabled(parentId != null);
        backButton.setOnClickListener(this);

        downloadButton = (Button) findViewById(R.id.download_button);
        downloadButton.setOnClickListener(this);

        searchText = (EditText) findViewById(R.id.search_text);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    executeSearch();
                    return true;
                }
                return false;
            }
        });
        searchButton = (ImageButton) findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);

        // Initialize credentials and service object.
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Collections.singletonList(DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff());

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        driveService = new Drive.Builder(transport, jsonFactory, credential)
                .setApplicationName("ODK-Collect")
                .build();

        getResultsFromApi();
    }

    /*
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     *
     * Google Drive API V3
     * Please refer to the below link for reference:
     * https://developers.google.com/drive/v3/web/quickstart/android
     */
    private void getResultsFromApi() {
        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            ToastUtils.showShortToast("No network connection available.");
        } else {
            if (isDeviceOnline()) {
                toDownload.clear();
                rootButton.setEnabled(false);
                searchButton.setEnabled(false);
                backButton.setEnabled(false);
                downloadButton.setEnabled(false);
                listFiles(ROOT_KEY);
                myDrive = !myDrive;
            } else {
                createAlertDialog(getString(R.string.no_connection));
            }
            currentPath.clear();
            currentPath.add((String) rootButton.getText());
        }
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

    void executeSearch() {
        String searchString = searchText.getText().toString();
        if (searchString.length() > 0) {
            toDownload.clear();
            searchButton.setEnabled(false);
            backButton.setEnabled(false);
            downloadButton.setEnabled(false);
            rootButton.setEnabled(false);
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
            currentPath.clear();
            listFiles(ROOT_KEY, searchText.getText().toString());
        } else {
            ToastUtils.showShortToast(R.string.no_blank_search);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(MY_DRIVE_KEY, myDrive);
        ArrayList<DriveListItem> dl = new ArrayList<DriveListItem>();
        for (int i = 0; i < listView.getCount(); i++) {
            dl.add((DriveListItem) listView.getItemAtPosition(i));
        }
        outState.putParcelableArrayList(DRIVE_ITEMS_KEY, dl);
        outState.putStringArray(PATH_KEY, currentPath.toArray(new String[currentPath.size()]));
        outState.putString(PARENT_KEY, parentId);
        outState.putBoolean(ALERT_SHOWING_KEY, alertShowing);
        outState.putString(ALERT_MSG_KEY, alertMsg);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapter.setEnabled(false);
        DriveListItem o = adapter.getItem(position);
        if (o != null && o.getType() == DriveListItem.DIR) {
            if (isDeviceOnline()) {
                toDownload.clear();
                searchText.setText(null);
                listFiles(o.getDriveId());
                folderIdStack.push(o.getDriveId());
                currentPath.push(o.getName());
            } else {
                adapter.setEnabled(true);
                createAlertDialog(getString(R.string.no_connection));
            }
        } else {
            adapter.setEnabled(true);
            // file clicked, download the file, mark checkbox.
            CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox);
            cb.setChecked(!cb.isChecked());

            if (toDownload.contains(o) && !cb.isChecked()) {
                toDownload.remove(o);
            } else {
                toDownload.add(o);
            }
            downloadButton.setEnabled(toDownload.size() > 0);
        }
    }

    private void getFiles() {
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < toDownload.size(); i++) {
            DriveListItem o = toDownload.get(i);
            messageBuilder.append(o.getName());
            if (i != toDownload.size() - 1) {
                messageBuilder.append(", ");
            }
        }

        alertMsg = getString(R.string.drive_get_file, messageBuilder.toString());
        showDialog(PROGRESS_DIALOG);

        getFileTask = new GetFileTask();
        getFileTask.setGoogleDriveFormDownloadListener(this);
        getFileTask.execute(toDownload);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                Collect.getInstance().getActivityLogger()
                        .logAction(this, "onCreateDialog.PROGRESS_DIALOG", "show");

                ProgressDialog progressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Collect.getInstance().getActivityLogger()
                                        .logAction(this, "onCreateDialog.PROGRESS_DIALOG",
                                                "cancel");
                                dialog.dismiss();
                                getFileTask.cancel(true);
                                getFileTask.setGoogleDriveFormDownloadListener(null);
                            }
                        };
                progressDialog.setTitle(getString(R.string.downloading_data));
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

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.download_forms_result));
        alertDialog.setMessage(message);
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // ok
                        Collect.getInstance().getActivityLogger()
                                .logAction(this, "createAlertDialog", "OK");
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
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
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
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;

            case COMPLETE_AUTHORIZATION_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (isDeviceOnline()) {
                        listFiles(ROOT_KEY);
                    } else {
                        createAlertDialog(getString(R.string.no_connection));
                    }
                } else {
                    // User denied access, show him the account chooser again
                }
                break;
        }
        if (resultCode == RESULT_CANCELED) {
            finish();
        }

    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (retrieveDriveFileContentsAsyncTask != null
                && retrieveDriveFileContentsAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            return retrieveDriveFileContentsAsyncTask;
        }
        return getFileTask;
    }

    private Stack<String> buildPath(String[] paths) {
        Stack<String> pathStack = new Stack<String>();
        for (String path : paths) {
            pathStack.push(path);
        }
        return pathStack;
    }

    @Override
    public void taskComplete(HashMap<String, Object> results) {
        rootButton.setEnabled(true);
        downloadButton.setEnabled(toDownload.size() > 0);
        searchButton.setEnabled(true);
        setProgressBarIndeterminateVisibility(false);

        if (results == null) {
            // if results was null, then got a google exception
            // requiring the user to authorize
            return;
        }

        String parentId = (String) results.get(PARENT_ID_KEY);

        if (myDrive) {
            rootButton.setText(getString(R.string.go_shared));
        } else {
            rootButton.setText(getString(R.string.go_drive));
        }

        if (folderIdStack.empty()) {
            backButton.setEnabled(false);
        } else {
            backButton.setEnabled(true);
        }
        this.parentId = parentId;

        if (currentPath.empty()) {
            if (myDrive) {
                currentPath.add(getString(R.string.go_drive));
            } else {
                currentPath.add(getString(R.string.go_shared));
            }
        }

    }

    @Override
    protected void onPause() {
        if (retrieveDriveFileContentsAsyncTask != null) {
            retrieveDriveFileContentsAsyncTask.setTaskListener(null);
        }
        if (getFileTask != null) {
            getFileTask.setGoogleDriveFormDownloadListener(null);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (retrieveDriveFileContentsAsyncTask != null) {
            retrieveDriveFileContentsAsyncTask.setTaskListener(this);
        }
        if (getFileTask != null) {
            getFileTask.setGoogleDriveFormDownloadListener(this);
        }
    }

    @Override
    public void formDownloadComplete(HashMap<String, Object> results) {
        try {
            dismissDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
            Timber.i("Exception thrown due to closing a dialog that was not open");
        }

        StringBuilder sb = new StringBuilder();

        for (String id : results.keySet()) {
            sb.append(id + " :: " + results.get(id) + "\n\n");
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        createAlertDialog(sb.toString());

    }

    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

    public void listFiles(String dir, String query) {
        setProgressBarIndeterminateVisibility(true);
        adapter = null;
        retrieveDriveFileContentsAsyncTask = new RetrieveDriveFileContentsAsyncTask();
        retrieveDriveFileContentsAsyncTask.setTaskListener(GoogleDriveActivity.this);
        if (query != null) {
            retrieveDriveFileContentsAsyncTask.execute(dir, query);
        } else {
            retrieveDriveFileContentsAsyncTask.execute(dir);

        }
    }

    public void listFiles(String dir) {
        listFiles(dir, null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.root_button:
                getResultsFromApi();
                break;

            case R.id.back_button:
                backButton.setEnabled(false);
                rootButton.setEnabled(false);
                downloadButton.setEnabled(false);
                toDownload.clear();
                if (isDeviceOnline()) {
                    if (folderIdStack.empty()) {
                        parentId = ROOT_KEY;
                    } else {
                        parentId = folderIdStack.pop();
                    }
                    listFiles(parentId);
                    currentPath.pop();
                    // }
                } else {
                    createAlertDialog(getString(R.string.no_connection));
                }
                break;

            case R.id.download_button:
                getFiles();
                break;

            case R.id.search_button:
                executeSearch();
                break;
        }
    }

    private class RetrieveDriveFileContentsAsyncTask extends
            AsyncTask<String, HashMap<String, Object>, HashMap<String, Object>> {
        private TaskListener listener;

        void setTaskListener(TaskListener tl) {
            listener = tl;
        }

        @Override
        protected HashMap<String, Object> doInBackground(String... params) {
            String currentDir = params[0];
            String query;

            if (rootId == null) {
                try {
                    rootId = driveService.files()
                            .get("root")
                            .setFields("id")
                            .execute().getId();
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), COMPLETE_AUTHORIZATION_REQUEST_CODE);
                    return null;
                } catch (IOException e) {
                    Timber.e(e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createAlertDialog(getString(R.string.google_auth_io_exception_msg));
                        }
                    });
                }
                if (rootId == null) {
                    Timber.e("Unable to fetch drive contents");
                    return null;
                }
            }

            Files.List request = null;
            String parentId = "";
            try {
                if (folderIdStack.empty()) {
                    parentId = rootId;
                } else {
                    parentId = folderIdStack.peek();
                }
                query = "'" + parentId + "' in parents";

                if (params.length == 2) {
                    // TODO: *.xml or .xml or xml
                    // then search mimetype
                    query = "fullText contains '" + params[1] + "' and trashed=false";
                }

                // SharedWithMe, and root:
                if (!myDrive && currentDir.equals(ROOT_KEY)) {
                    query = "sharedWithMe=true";
                    folderIdStack.removeAllElements();
                }

                query += " and trashed=false";
                request = driveService.files().list().setQ(query);
            } catch (IOException e) {
                Timber.e(e);
            }
            request.setFields("nextPageToken, files(modifiedTime, id, name, mimeType)");

            HashMap<String, Object> results = new HashMap<>();
            results.put(PARENT_ID_KEY, parentId);
            results.put(CURRENT_ID_KEY, currentDir);
            do {
                try {
                    FileList fa = request.execute();
                    List<com.google.api.services.drive.model.File> driveFileListPage =
                            new ArrayList<>();

                    driveFileListPage.addAll(fa.getFiles());
                    request.setPageToken(fa.getNextPageToken());
                    HashMap<String, Object> nextPage = new HashMap<>();
                    nextPage.put(PARENT_ID_KEY, parentId);
                    nextPage.put(CURRENT_ID_KEY, currentDir);
                    nextPage.put(FILE_LIST_KEY, driveFileListPage);
                    publishProgress(nextPage);
                } catch (IOException e) {
                    Timber.e(e, "Exception thrown while accessing the file list");
                }
            } while (request.getPageToken() != null && request.getPageToken().length() > 0);

            return results;

        }

        @Override
        protected void onPostExecute(HashMap<String, Object> results) {
            super.onPostExecute(results);
            if (results == null) {
                // was an auth request
                return;
            }
            if (listener != null) {
                listener.taskComplete(results);
            }
        }

        @SafeVarargs
        @Override
        protected final void onProgressUpdate(HashMap<String, Object>... values) {
            super.onProgressUpdate(values);
            List<com.google.api.services.drive.model.File> fileList =
                    (List<com.google.api.services.drive.model.File>) values[0]
                            .get(FILE_LIST_KEY);
            String parentId = (String) values[0].get(PARENT_ID_KEY);
            String currentDir = (String) values[0].get(CURRENT_ID_KEY);

            List<DriveListItem> dirs = new ArrayList<>();
            List<DriveListItem> forms = new ArrayList<>();

            for (com.google.api.services.drive.model.File f : fileList) {
                String type = f.getMimeType();
                switch (type) {
                    case "application/xml":
                    case "text/xml":
                    case "application/xhtml":
                    case "text/xhtml":
                    case "application/xhtml+xml":
                        forms.add(new DriveListItem(f.getName(), "", f.getModifiedTime(), "", "",
                                DriveListItem.FILE, f.getId(), currentDir));
                        break;
                    case "application/vnd.google-apps.folder":
                        dirs.add(new DriveListItem(f.getName(), "", f.getModifiedTime(), "", "",
                                DriveListItem.DIR, f.getId(), parentId));
                        break;
                    default:
                        // skip the rest of the files
                        break;
                }
            }
            Collections.sort(dirs);
            Collections.sort(forms);
            dirs.addAll(forms);

            if (dirs.size() == 0) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.INVISIBLE);
            }

            if (adapter == null) {
                adapter = new FileArrayAdapter(GoogleDriveActivity.this, R.layout.two_item_image,
                        dirs);
                listView.setAdapter(adapter);
            } else {
                for (DriveListItem d : dirs) {
                    adapter.add(d);
                }
                adapter.addAll(dirs);
            }
            adapter.sort(new Comparator<DriveListItem>() {
                @Override
                public int compare(DriveListItem lhs, DriveListItem rhs) {
                    if (lhs.getType() != rhs.getType()) {
                        if (lhs.getType() == DriveListItem.DIR) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                }
            });
            adapter.notifyDataSetChanged();
        }
    }

    private class GetFileTask extends
            AsyncTask<ArrayList<DriveListItem>, Boolean, HashMap<String, Object>> {

        private GoogleDriveFormDownloadListener listener;

        void setGoogleDriveFormDownloadListener(GoogleDriveFormDownloadListener gl) {
            listener = gl;
        }

        @SafeVarargs
        @Override
        protected final HashMap<String, Object> doInBackground(ArrayList<DriveListItem>... params) {
            HashMap<String, Object> results = new HashMap<>();

            ArrayList<DriveListItem> fileItems = params[0];

            for (int k = 0; k < fileItems.size(); k++) {
                DriveListItem fileItem = fileItems.get(k);

                FileOutputStream fileOutputStream = null;
                try {
                    com.google.api.services.drive.model.File df = driveService.files()
                            .get(fileItem.getDriveId()).execute();

                    fileOutputStream = new FileOutputStream(
                            new File(Collect.FORMS_PATH + File.separator + fileItem.getName()));
                    downloadFile(df).writeTo(fileOutputStream);

                    String mediaDirName = fileItem.getName().substring(0, fileItem.getName().length() - 4) + "-media";

                    String requestString = "'" + fileItem.getParentId() + "' in parents and trashed=false and name='" + mediaDirName + "'";
                    Files.List request;
                    List<com.google.api.services.drive.model.File> driveFileList = new ArrayList<>();

                    try {
                        request = driveService.files().list().setQ(requestString);
                    } catch (IOException e) {
                        Timber.e(e);
                        results.put(fileItem.getName(), e.getMessage());
                        return results;
                    }
                    do {
                        try {
                            FileList fa = request.execute();
                            driveFileList.addAll(fa.getFiles());
                            request.setPageToken(fa.getNextPageToken());
                        } catch (Exception e) {
                            Timber.e(e);
                            results.put(fileItem.getName(), e.getMessage());
                            return results;
                        }
                    } while (request.getPageToken() != null && request.getPageToken().length() > 0);

                    if (driveFileList.size() > 1) {
                        results.put(fileItem.getName(), getString(R.string.multiple_media_folders_detected_notification));
                        return results;
                    } else if (driveFileList.size() == 1) {
                        requestString = "'" + driveFileList.get(0).getId() + "' in parents and trashed=false";
                        List<com.google.api.services.drive.model.File> mediaFileList = new ArrayList<>();

                        try {
                            request = driveService.files().list().setQ(requestString);
                            do {
                                try {
                                    FileList fa = request.execute();
                                    mediaFileList.addAll(fa.getFiles());
                                    request.setPageToken(fa.getNextPageToken());
                                } catch (Exception e) {
                                    Timber.e(e);
                                    results.put(fileItem.getName(), e.getMessage());
                                    return results;
                                }
                            } while (request.getPageToken() != null && request.getPageToken().length() > 0);
                        } catch (Exception e) {
                            Timber.e(e);
                            results.put(fileItem.getName(), e.getMessage());
                            return results;
                        }

                        File mediaDir = new File(Collect.FORMS_PATH + File.separator + mediaDirName);
                        if (!mediaDir.exists()) {
                            mediaDir.mkdir();
                        }

                        for (com.google.api.services.drive.model.File mediaFile : mediaFileList) {
                            fileOutputStream = new FileOutputStream(
                                    new File(Collect.FORMS_PATH + File.separator + mediaDirName + File.separator + mediaFile.getName()));
                            downloadFile(mediaFile).writeTo(fileOutputStream);
                            results.put(mediaDirName + File.separator + mediaFile.getName(), Collect.getInstance().getString(R.string.success));
                        }
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    results.put(fileItem.getName(), e.getMessage());
                    return results;
                } finally {
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        Timber.e(e, "Unable to close the file output stream");
                    }
                }

                results.put(fileItem.getName(), Collect.getInstance().getString(R.string.success));
            }
            return results;

        }

        private ByteArrayOutputStream downloadFile(com.google.api.services.drive.model.File file)
                throws IOException {

            String fileId = file.getId();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
            return outputStream;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> results) {
            listener.formDownloadComplete(results);
        }
    }
}
