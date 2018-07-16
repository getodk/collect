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

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.FileArrayAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.MultipleFoldersFoundException;
import org.odk.collect.android.listeners.GoogleDriveFormDownloadListener;
import org.odk.collect.android.listeners.TaskListener;
import org.odk.collect.android.logic.DriveListItem;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.gdrive.DriveHelper;
import org.odk.collect.android.utilities.gdrive.GoogleAccountsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import timber.log.Timber;

import static org.odk.collect.android.utilities.gdrive.GoogleAccountsManager.REQUEST_ACCOUNT_PICKER;

public class GoogleDriveActivity extends FormListActivity implements View.OnClickListener,
        TaskListener, GoogleDriveFormDownloadListener, GoogleAccountsManager.GoogleAccountSelectionListener,
        AdapterView.OnItemClickListener {

    private static final String DRIVE_DOWNLOAD_LIST_SORTING_ORDER = "driveDownloadListSortingOrder";
    public static final int AUTHORIZATION_REQUEST_CODE = 4322;
    private static final int PROGRESS_DIALOG = 1;
    private static final int GOOGLE_USER_DIALOG = 3;
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
    private Button rootButton;
    private Button backButton;
    private Button downloadButton;
    private Stack<String> currentPath = new Stack<>();
    private final Stack<String> folderIdStack = new Stack<>();
    private String alertMsg;
    private boolean alertShowing;
    private String rootId;
    private boolean myDrive;
    private FileArrayAdapter adapter;
    private RetrieveDriveFileContentsAsyncTask retrieveDriveFileContentsAsyncTask;
    private GetFileTask getFileTask;
    private String parentId;
    private ArrayList<DriveListItem> toDownload;
    private List<DriveListItem> filteredList;
    private List<DriveListItem> driveList;
    private DriveHelper driveHelper;
    private GoogleAccountsManager accountsManager;

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.google_drive));
        setSupportActionBar(toolbar);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drive_layout);

        setProgressBarVisibility(true);
        initToolbar();

        parentId = null;
        alertShowing = false;
        toDownload = new ArrayList<>();
        filteredList = new ArrayList<>();
        driveList = new ArrayList<>();

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
            filteredList.addAll(dl);
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

        rootButton = findViewById(R.id.root_button);
        if (myDrive) {
            rootButton.setText(getString(R.string.go_shared));
        } else {
            rootButton.setText(getString(R.string.go_drive));
        }
        rootButton.setOnClickListener(this);

        backButton = findViewById(R.id.back_button);
        backButton.setEnabled(parentId != null);
        backButton.setOnClickListener(this);

        downloadButton = findViewById(R.id.download_button);
        downloadButton.setOnClickListener(this);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);

        sortingOptions = new String[]{
                getString(R.string.sort_by_name_asc), getString(R.string.sort_by_name_desc)
        };

        accountsManager = new GoogleAccountsManager(this);
        accountsManager.setListener(this);
        driveHelper = accountsManager.getDriveHelper();
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
        if (!accountsManager.isGoogleAccountSelected()) {
            accountsManager.chooseAccount();
        } else {
            if (isDeviceOnline()) {
                toDownload.clear();
                filteredList.clear();
                driveList.clear();
                folderIdStack.clear();
                rootButton.setEnabled(false);
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        accountsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(MY_DRIVE_KEY, myDrive);
        ArrayList<DriveListItem> dl = new ArrayList<>();
        dl.addAll(filteredList);
        outState.putParcelableArrayList(DRIVE_ITEMS_KEY, dl);
        outState.putStringArray(PATH_KEY, currentPath.toArray(new String[currentPath.size()]));
        outState.putString(PARENT_KEY, parentId);
        outState.putBoolean(ALERT_SHOWING_KEY, alertShowing);
        outState.putString(ALERT_MSG_KEY, alertMsg);
        super.onSaveInstanceState(outState);
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
    protected void updateAdapter() {
        CharSequence charSequence = getFilterText();
        filteredList.clear();

        if (charSequence.length() > 0) {
            for (DriveListItem item : driveList) {
                if (item.getName().toLowerCase(Locale.US).contains(charSequence.toString().toLowerCase(Locale.US))) {
                    filteredList.add(item);
                }
            }
        } else {
            filteredList.addAll(driveList);
        }

        sortList();
        if (adapter == null) {
            adapter = new FileArrayAdapter(this, filteredList);
            listView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        adapter.notifyDataSetChanged();
        checkPreviouslyCheckedItems();
    }

    @Override
    protected void checkPreviouslyCheckedItems() {
        listView.clearChoices();
        for (int i = 0; i < listView.getCount(); i++) {
            DriveListItem item = (DriveListItem) listView.getAdapter().getItem(i);
            if (toDownload.contains(item)) {
                listView.setItemChecked(i, true);
            }
        }
    }

    private void sortList() {
        Collections.sort(filteredList, (lhs, rhs) -> {
            if (lhs.getType() != rhs.getType()) {
                return lhs.getType() == DriveListItem.DIR ? -1 : 1;
            } else {
                int compareName = lhs.getName().compareToIgnoreCase(rhs.getName());
                return getSortingOrder().equals(SORT_BY_NAME_ASC) ? compareName : -compareName;
            }
        });
    }

    @Override
    protected String getSortingOrderKey() {
        return DRIVE_DOWNLOAD_LIST_SORTING_ORDER;
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
                gudBuilder.setPositiveButton(R.string.ok, (dialog, which) -> finish());
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
        DialogUtils.showDialog(alertDialog, this);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    accountsManager.setSelectedAccountName(accountName);
                }
                break;
            case AUTHORIZATION_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
        if (resultCode == RESULT_CANCELED) {
            Timber.d("AUTHORIZE_DRIVE_ACCESS failed, asking to choose new account:");
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
        downloadButton.setEnabled(!toDownload.isEmpty());
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
            sb.append(id).append(" :: ").append(results.get(id)).append("\n\n");
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

    @Override
    protected void onDestroy() {
        if (retrieveDriveFileContentsAsyncTask != null) {
            if (!retrieveDriveFileContentsAsyncTask.isCancelled()) {
                retrieveDriveFileContentsAsyncTask.cancel(true);
            }
            retrieveDriveFileContentsAsyncTask.setTaskListener(null);
        }
        if (getFileTask != null) {
            if (!getFileTask.isCancelled()) {
                getFileTask.cancel(true);
            }
            getFileTask.setGoogleDriveFormDownloadListener(null);
        }
        finish();
        super.onDestroy();
    }

    public void listFiles(String dir, String query) {
        setProgressBarIndeterminateVisibility(true);
        adapter = null;
        retrieveDriveFileContentsAsyncTask = new RetrieveDriveFileContentsAsyncTask();
        retrieveDriveFileContentsAsyncTask.setTaskListener(this);
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
                driveList.clear();
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
        }
    }

    @Override
    public void onGoogleAccountSelected(String accountName) {
        getResultsFromApi();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DriveListItem item = filteredList.get(position);
        if (item != null && item.getType() == DriveListItem.DIR) {
            if (isDeviceOnline()) {
                toDownload.clear();
                driveList.clear();
                clearSearchView();
                listFiles(item.getDriveId());
                folderIdStack.push(item.getDriveId());
                currentPath.push(item.getName());
            } else {
                createAlertDialog(getString(R.string.no_connection));
            }
        } else {
            // file clicked, download the file, mark checkbox.
            CheckBox cb = view.findViewById(R.id.checkbox);
            cb.setChecked(!cb.isChecked());
            item.setSelected(cb.isChecked());

            if (toDownload.contains(item) && !cb.isChecked()) {
                toDownload.remove(item);
            } else {
                toDownload.add(item);
            }
            downloadButton.setEnabled(!toDownload.isEmpty());
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
            if (rootId == null) {
                try {
                    rootId = driveHelper.getRootFolderId();
                } catch (UserRecoverableAuthIOException e) {
                    GoogleDriveActivity.this.startActivityForResult(e.getIntent(), AUTHORIZATION_REQUEST_CODE);
                } catch (IOException e) {
                    Timber.e(e);
                    runOnUiThread(() -> createAlertDialog(getString(R.string.google_auth_io_exception_msg)));
                }
                if (rootId == null) {
                    Timber.e("Unable to fetch drive contents");
                    return null;
                }
            }

            String parentId;
            if (folderIdStack.empty()) {
                parentId = rootId;
            } else {
                parentId = folderIdStack.peek();
            }
            String query = "'" + parentId + "' in parents";

            if (params.length == 2) {
                // TODO: *.xml or .xml or xml
                // then search mimetype
                query = "fullText contains '" + params[1] + "'";
            }

            // SharedWithMe, and root:
            String currentDir = params[0];

            if (!myDrive) {
                if (currentDir.equals(ROOT_KEY) || folderIdStack.empty()) {
                    query = "sharedWithMe=true";
                }
            }

            query += " and trashed=false";

            String fields = "nextPageToken, files(modifiedTime, id, name, mimeType)";
            Drive.Files.List request = null;
            try {
                request = driveHelper.buildRequest(query, fields);
            } catch (IOException e) {
                Timber.e(e);
            }

            HashMap<String, Object> results = new HashMap<>();
            results.put(PARENT_ID_KEY, parentId);
            results.put(CURRENT_ID_KEY, currentDir);
            if (request != null) {
                List<com.google.api.services.drive.model.File> driveFileListPage;
                do {
                    try {
                        driveFileListPage = new ArrayList<>();
                        driveHelper.fetchFilesForCurrentPage(request, driveFileListPage);

                        HashMap<String, Object> nextPage = new HashMap<>();
                        nextPage.put(PARENT_ID_KEY, parentId);
                        nextPage.put(CURRENT_ID_KEY, currentDir);
                        nextPage.put(FILE_LIST_KEY, driveFileListPage);
                        publishProgress(nextPage);
                    } catch (IOException e) {
                        Timber.e(e, "Exception thrown while accessing the file list");
                    }
                } while (request.getPageToken() != null && request.getPageToken().length() > 0);
            }
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
            driveList.addAll(dirs);
            driveList.addAll(forms);
            updateAdapter();
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

                try {
                    downloadFile(fileItem.getDriveId(), fileItem.getName());
                    results.put(fileItem.getName(), Collect.getInstance().getString(R.string.success));

                    String mediaDirName = FileUtils.constructMediaPath(fileItem.getName());

                    String folderId;
                    try {
                        folderId = driveHelper.getIDOfFolderWithName(mediaDirName, fileItem.getParentId(), false);
                    } catch (MultipleFoldersFoundException exception) {
                        results.put(fileItem.getName(), getString(R.string.multiple_media_folders_detected_notification));
                        return results;
                    }

                    if (folderId != null) {
                        List<com.google.api.services.drive.model.File> mediaFileList;
                        mediaFileList = driveHelper.getFilesFromDrive(null, folderId);

                        FileUtils.createFolder(Collect.FORMS_PATH + File.separator + mediaDirName);

                        for (com.google.api.services.drive.model.File mediaFile : mediaFileList) {
                            String filePath = mediaDirName + File.separator + mediaFile.getName();
                            downloadFile(mediaFile.getId(), filePath);
                            results.put(filePath, Collect.getInstance().getString(R.string.success));
                        }
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    results.put(fileItem.getName(), e.getMessage());
                    return results;
                }
            }
            return results;
        }

        private void downloadFile(@NonNull String fileId, String fileName) throws IOException {
            File file = new File(Collect.FORMS_PATH + File.separator + fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            driveHelper.downloadFile(fileId, fileOutputStream);
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> results) {
            listener.formDownloadComplete(results);
        }
    }
}
