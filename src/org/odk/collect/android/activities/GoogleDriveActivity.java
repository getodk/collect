/*
 * Copyright (C) 2014 Nafundi
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.FileArrayAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.GoogleDriveFormDownloadListener;
import org.odk.collect.android.listeners.TaskListener;
import org.odk.collect.android.logic.DriveListItem;
import org.odk.collect.android.preferences.PreferencesActivity;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentList;

public class GoogleDriveActivity extends ListActivity implements OnConnectionFailedListener,
        TaskListener, GoogleDriveFormDownloadListener {

    private final static int PROGRESS_DIALOG = 1;

    private final static int GOOGLE_USER_DIALOG = 3;

    private ProgressDialog mProgressDialog;

    private AlertDialog mAlertDialog;

    private Button mRootButton;

    private Button mBackButton;

    private Button mDownloadButton;

    private ImageButton mSearchButton;

    private EditText mSearchText;

    private Stack<String> mCurrentPath = new Stack<String>();

    private String mAlertMsg;

    private boolean mAlertShowing;

    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 5555;

    private static final int COMPLETE_AUTHORIZATION_REQUEST_CODE = 4322;

    private String rootId = null;

    private boolean MyDrive;

    private FileArrayAdapter adapter;

    private RetrieveDriveFileContentsAsyncTask mRetrieveDriveFileContentsAsyncTask;

    private GetFileTask mGetFileTask;

    private String mGoogleUsername = null;

    private String mParentId;

    private ArrayList<DriveListItem> toDownload;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.google_drive));
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setProgressBarVisibility(true);
        setContentView(R.layout.drive_layout);

        mParentId = null;
        mAlertShowing = false;
        toDownload = new ArrayList<DriveListItem>();

        // ensure we have a google account selected
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mGoogleUsername = prefs.getString(PreferencesActivity.KEY_SELECTED_GOOGLE_ACCOUNT, null);
        if (mGoogleUsername == null || mGoogleUsername.equals("")) {
            showDialog(GOOGLE_USER_DIALOG);
            return;
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(MY_DRIVE_KEY)) {
            // recover state on rotate
            MyDrive = savedInstanceState.getBoolean(MY_DRIVE_KEY);
            String[] patharray = savedInstanceState.getStringArray(PATH_KEY);
            mCurrentPath = buildPath(patharray);

            TextView empty = (TextView)findViewById(android.R.id.empty);
            getListView().setEmptyView(empty);

            mParentId = savedInstanceState.getString(PARENT_KEY);
            mAlertMsg = savedInstanceState.getString(ALERT_MSG_KEY);
            mAlertShowing = savedInstanceState.getBoolean(ALERT_SHOWING_KEY);

            ArrayList<DriveListItem> dl = savedInstanceState
                    .getParcelableArrayList(DRIVE_ITEMS_KEY);
            adapter = new FileArrayAdapter(GoogleDriveActivity.this, R.layout.two_item_image, dl);
            setListAdapter(adapter);
        } else {
            // new
            TextView emptyView = new TextView(this);
            emptyView.setText(getString(R.string.gme_search_browse));
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setTextSize(21);

            getListView().getEmptyView().setVisibility(View.INVISIBLE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            ((ViewGroup)getListView().getParent()).addView(emptyView, lp);
            getListView().setEmptyView(emptyView);

            MyDrive = false;

            if (testNetwork()) {
            } else {
                createAlertDialog(getString(R.string.no_connection));
            }
        }

        // restore any task state
        if (getLastNonConfigurationInstance() instanceof RetrieveDriveFileContentsAsyncTask) {
            mRetrieveDriveFileContentsAsyncTask = (RetrieveDriveFileContentsAsyncTask)getLastNonConfigurationInstance();
            setProgressBarIndeterminateVisibility(true);
        } else {
            mGetFileTask = (GetFileTask)getLastNonConfigurationInstance();
            if (mGetFileTask != null) {
                mGetFileTask.setGoogleDriveFormDownloadListener(this);
            }
        }
        if (mGetFileTask != null && mGetFileTask.getStatus() == AsyncTask.Status.FINISHED) {
            try {
                dismissDialog(PROGRESS_DIALOG);
            } catch (Exception e) {
                e.printStackTrace();
                // don't care...
            }
        }
        if (mAlertShowing) {
            try {
                dismissDialog(PROGRESS_DIALOG);
            } catch (Exception e) {
                e.printStackTrace();
                // don't care...
            }
            createAlertDialog(mAlertMsg);
        }

        mRootButton = (Button)findViewById(R.id.root_button);
        if (MyDrive) {
            mRootButton.setText(getString(R.string.go_shared));
        } else {
            mRootButton.setText(getString(R.string.go_drive));
        }
        mRootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (testNetwork()) {
                    toDownload.clear();
                    mRootButton.setEnabled(false);
                    mSearchButton.setEnabled(false);
                    mBackButton.setEnabled(false);
                    mDownloadButton.setEnabled(false);
                    listFiles(ROOT_KEY);
                    MyDrive = !MyDrive;
                } else {
                    createAlertDialog(getString(R.string.no_connection));
                }
                mCurrentPath.clear();
                mCurrentPath.add((String)mRootButton.getText());
            }
        });

        mBackButton = (Button)findViewById(R.id.back_button);
        mBackButton.setEnabled(mParentId != null);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackButton.setEnabled(false);
                mRootButton.setEnabled(false);
                mDownloadButton.setEnabled(false);
                toDownload.clear();
                getListView().getEmptyView().setVisibility(View.INVISIBLE);
                TextView empty = (TextView)findViewById(android.R.id.empty);
                empty.setVisibility(View.VISIBLE);
                getListView().setEmptyView(empty);
                if (testNetwork()) {
                    if (mParentId == null) {
                        mParentId = ROOT_KEY;
                    }
                    listFiles(mParentId);
                    mCurrentPath.pop();
                    // }
                } else {
                    createAlertDialog(getString(R.string.no_connection));
                }
            }
        });

        mDownloadButton = (Button)findViewById(R.id.download_button);
        mDownloadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getFiles();
            }
        });

        mSearchText = (EditText)findViewById(R.id.search_text);
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    executeSearch();
                    return true;
                }
                return false;
            }
        });
        mSearchButton = (ImageButton)findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                executeSearch();
            }
        });

    }
    
    void executeSearch() {
        String searchString = mSearchText.getText().toString();
        if (searchString.length() > 0) {
            toDownload.clear();
            mSearchButton.setEnabled(false);
            mBackButton.setEnabled(false);
            mDownloadButton.setEnabled(false);
            mRootButton.setEnabled(false);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
            mCurrentPath.clear();
            listFiles(ROOT_KEY, mSearchText.getText().toString());
        } else {
            Toast.makeText(this, R.string.no_blank_search, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(MY_DRIVE_KEY, MyDrive);
        ArrayList<DriveListItem> dl = new ArrayList<DriveListItem>();
        for (int i = 0; i < getListView().getCount(); i++) {
            dl.add((DriveListItem)getListView().getItemAtPosition(i));
        }
        outState.putParcelableArrayList(DRIVE_ITEMS_KEY, dl);
        outState.putStringArray(PATH_KEY, mCurrentPath.toArray(new String[mCurrentPath.size()]));
        outState.putString(PARENT_KEY, mParentId);
        outState.putBoolean(ALERT_SHOWING_KEY, mAlertShowing);
        outState.putString(ALERT_MSG_KEY, mAlertMsg);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        getListView().getEmptyView().setVisibility(View.INVISIBLE);
        TextView empty = (TextView)findViewById(android.R.id.empty);
        empty.setVisibility(View.VISIBLE);
        getListView().setEmptyView(empty);

        DriveListItem o = adapter.getItem(position);
        if (o != null && o.getType() == DriveListItem.DIR) {
            if (testNetwork()) {
                toDownload.clear();
                mSearchText.setText(null);
                listFiles(o.getDriveId());
                mCurrentPath.push(o.getName());
            } else {
                createAlertDialog(getString(R.string.no_connection));
            }
        } else {
            // file clicked, download the file, mark checkbox.
            CheckBox cb = (CheckBox)v.findViewById(R.id.checkbox);
            cb.setChecked(!cb.isChecked());

            if (toDownload.contains(o) && !cb.isChecked()) {
                toDownload.remove(o);
            } else {
                toDownload.add(o);
            }
            mDownloadButton.setEnabled(toDownload.size() > 0);
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

        mAlertMsg = getString(R.string.drive_get_file, messageBuilder.toString());
        showDialog(PROGRESS_DIALOG);

        mGetFileTask = new GetFileTask();
        mGetFileTask.setGoogleDriveFormDownloadListener(this);
        mGetFileTask.execute(toDownload);
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

                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Collect.getInstance().getActivityLogger()
                                .logAction(this, "onCreateDialog.PROGRESS_DIALOG", "cancel");
                        dialog.dismiss();
                        mGetFileTask.cancel(true);
                        mGetFileTask.setGoogleDriveFormDownloadListener(null);
                    }
                };
                mProgressDialog.setTitle(getString(R.string.downloading_data));
                mProgressDialog.setMessage(mAlertMsg);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
            case GOOGLE_USER_DIALOG:
                AlertDialog.Builder gudBuilder = new AlertDialog.Builder(this);

                gudBuilder.setTitle(getString(R.string.no_google_account));
                gudBuilder.setMessage(getString(R.string.gme_set_account));
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

        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setTitle(getString(R.string.download_forms_result));
        mAlertDialog.setMessage(message);
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // ok
                        Collect.getInstance().getActivityLogger()
                                .logAction(this, "createAlertDialog", "OK");
                        mAlertShowing = false;
                        finish();
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), quitListener);
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertShowing = true;
        mAlertMsg = message;
        mAlertDialog.show();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case COMPLETE_AUTHORIZATION_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (testNetwork()) {
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
    public Object onRetainNonConfigurationInstance() {
        if (mRetrieveDriveFileContentsAsyncTask != null
                && mRetrieveDriveFileContentsAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            return mRetrieveDriveFileContentsAsyncTask;
        }
        return mGetFileTask;
    }

    // List<com.google.api.services.drive.model.File>
    private class RetrieveDriveFileContentsAsyncTask extends
            AsyncTask<String, HashMap<String, Object>, HashMap<String, Object>> {

        private TaskListener listener;

        public void setTaskListener(TaskListener tl) {
            listener = tl;
        }

        @Override
        protected HashMap<String, Object> doInBackground(String... params) {
            HashMap<String, Object> results = new HashMap<String, Object>();

            String currentDir = params[0];
            String parentId = ROOT_KEY;
            String query = null;
            if (params.length == 2) {
                // TODO: *.xml or .xml or xml
                // then search mimetype
                query = "title contains '" + params[1] + "' and trashed=false";
            }

            Collection<String> collection = new ArrayList<String>();
            collection.add(com.google.api.services.drive.DriveScopes.DRIVE);
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    GoogleDriveActivity.this.getApplicationContext(), collection);

            com.google.api.services.drive.Drive service = new com.google.api.services.drive.Drive.Builder(
                    AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
            credential.setSelectedAccountName(mGoogleUsername);

            if (rootId == null) {
                com.google.api.services.drive.model.File rootfile = null;
                try {
                    rootfile = service.files().get(ROOT_KEY).execute();
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), COMPLETE_AUTHORIZATION_REQUEST_CODE);
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                rootId = rootfile.getId();
            }

            String requestString = "'" + currentDir + "' in parents and trashed=false";
            Files.List request = null;

            try {
                ParentList parents = service.parents().list(currentDir).execute();
                if (parents.getItems().size() > 0) {
                    parentId = parents.getItems().get(0).getId();
                    if (parents.getItems().get(0).getIsRoot()) {
                        rootId = parentId;
                    }
                } else {
                    parentId = null;
                }

                // Sharedwithme, and root:
                if (!MyDrive && currentDir.equals(ROOT_KEY)) {
                    requestString = "trashed=false and sharedWithMe=true";
                }

                request = service.files().list().setQ(requestString);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            // If there's a query parameter, we're searching for all the files.
            if (query != null) {
                try {
                    request = service.files().list().setQ(query);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            results.put(PARENT_ID_KEY, parentId);
            results.put(CURRENT_ID_KEY, currentDir);
            do {
                try {
                    FileList fa = request.execute();
                    List<com.google.api.services.drive.model.File> driveFileListPage = new ArrayList<com.google.api.services.drive.model.File>();

                    driveFileListPage.addAll(fa.getItems());
                    request.setPageToken(fa.getNextPageToken());
                    HashMap<String, Object> nextPage = new HashMap<String, Object>();
                    nextPage.put(PARENT_ID_KEY, parentId);
                    nextPage.put(CURRENT_ID_KEY, currentDir);
                    nextPage.put(FILE_LIST_KEY, driveFileListPage);
                    publishProgress(nextPage);
                } catch (IOException e) {
                    e.printStackTrace();
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

        @Override
        protected void onProgressUpdate(HashMap<String, Object>... values) {
            super.onProgressUpdate(values);

            List<com.google.api.services.drive.model.File> fileList = (List<com.google.api.services.drive.model.File>)values[0]
                    .get(FILE_LIST_KEY);
            String parentId = (String)values[0].get(PARENT_ID_KEY);
            String currentDir = (String)values[0].get(CURRENT_ID_KEY);

            List<DriveListItem> dirs = new ArrayList<DriveListItem>();
            List<DriveListItem> forms = new ArrayList<DriveListItem>();

            for (com.google.api.services.drive.model.File f : fileList) {
                if (f.getMimeType().equals("application/xml")) {
                    forms.add(new DriveListItem(f.getTitle(), "", f.getModifiedDate(), "", "",
                            DriveListItem.FILE, f.getId(), currentDir));
                } else if (f.getMimeType().equals("application/xhtml")) {
                    forms.add(new DriveListItem(f.getTitle(), "", f.getModifiedDate(), "", "",
                            DriveListItem.FILE, f.getId(), currentDir));
                } else if (f.getMimeType().equals("application/vnd.google-apps.folder")) {
                    dirs.add(new DriveListItem(f.getTitle(), "", f.getModifiedDate(), "", "",
                            DriveListItem.DIR, f.getId(), parentId));
                } else {
                    // skip the rest of the files
                }
            }
            Collections.sort(dirs);
            Collections.sort(forms);
            dirs.addAll(forms);

            if (adapter == null) {
                adapter = new FileArrayAdapter(GoogleDriveActivity.this, R.layout.two_item_image,
                        dirs);
                GoogleDriveActivity.this.setListAdapter(adapter);
            } else {
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

    private Stack<String> buildPath(String[] path) {
        Stack<String> pathStack = new Stack<String>();
        for (int i = 0; i < path.length; i++) {
            pathStack.push(path[i]);
        }
        return pathStack;
    }

    private class GetFileTask extends
            AsyncTask<ArrayList<DriveListItem>, Boolean, HashMap<String, Object>> {

        private GoogleDriveFormDownloadListener listener;

        public void setGoogleDriveFormDownloadListener(GoogleDriveFormDownloadListener gl) {
            listener = gl;
        }

        @Override
        protected HashMap<String, Object> doInBackground(ArrayList<DriveListItem>... params) {
            HashMap<String, Object> results = new HashMap<String, Object>();

            ArrayList<DriveListItem> fileItems = params[0];

            Collection<String> collection = new ArrayList<String>();
            collection.add(com.google.api.services.drive.DriveScopes.DRIVE);
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    GoogleDriveActivity.this.getApplicationContext(), collection);

            com.google.api.services.drive.Drive service = new com.google.api.services.drive.Drive.Builder(
                    AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
            credential.setSelectedAccountName(mGoogleUsername);

            for (int k = 0; k < fileItems.size(); k++) {
                DriveListItem fileItem = fileItems.get(k);

                String mediaDir = fileItem.getName().substring(0, fileItem.getName().length() - 4)
                        + "-media";

                String requestString = "'" + fileItem.getParentId()
                        + "' in parents and trashed=false and title='" + mediaDir + "'";
                Files.List request = null;
                List<com.google.api.services.drive.model.File> driveFileList = new ArrayList<com.google.api.services.drive.model.File>();

                try {
                    request = service.files().list().setQ(requestString);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    results.put(fileItem.getName(), e1.getMessage());
                    return results;
                }
                do {
                    try {
                        FileList fa = request.execute();
                        driveFileList.addAll(fa.getItems());
                        request.setPageToken(fa.getNextPageToken());
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        results.put(fileItem.getName(), e2.getMessage());
                        return results;
                    }
                } while (request.getPageToken() != null && request.getPageToken().length() > 0);

                if (driveFileList.size() > 1) {
                    results.put(fileItem.getName(),
                            "More than one media folder detected, please remove one and try again");
                    return results;
                } else if (driveFileList.size() == 1) {
                    requestString = "'" + driveFileList.get(0).getId()
                            + "' in parents and trashed=false";
                    List<com.google.api.services.drive.model.File> mediaFileList = new ArrayList<com.google.api.services.drive.model.File>();

                    try {
                        request = service.files().list().setQ(requestString);
                        do {
                            try {
                                FileList fa = request.execute();
                                mediaFileList.addAll(fa.getItems());
                                request.setPageToken(fa.getNextPageToken());
                            } catch (Exception e2) {
                                e2.printStackTrace();
                                results.put(fileItem.getName(), e2.getMessage());
                                return results;
                            }
                        } while (request.getPageToken() != null
                                && request.getPageToken().length() > 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        results.put(fileItem.getName(), e.getMessage());
                        return results;
                    }
                } else {
                    // zero.. just downloda the .xml file
                }

                try {
                    com.google.api.services.drive.model.File df = service.files()
                            .get(fileItem.getDriveId()).execute();

                    InputStream is = downloadFile(service, df);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    FileWriter fw = new FileWriter(Collect.FORMS_PATH + File.separator
                            + fileItem.getName());

                    int c;
                    while ((c = reader.read()) != -1) {
                        fw.write(c);
                    }

                    fw.flush();
                    fw.close();
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    results.put(fileItem.getName(), e.getMessage());
                    return results;
                }

                results.put(fileItem.getName(), Collect.getInstance().getString(R.string.success));
            }
            return results;

        }

        private InputStream downloadFile(com.google.api.services.drive.Drive service,
                com.google.api.services.drive.model.File file) {
            if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
                try {
                    HttpResponse resp = service.getRequestFactory()
                            .buildGetRequest(new GenericUrl(file.getDownloadUrl())).execute();
                    return resp.getContent();
                } catch (IOException e) {
                    // An error occurred.
                    e.printStackTrace();
                    return null;
                }
            } else {
                // The file doesn't have any content stored on Drive.
                return null;
            }
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> results) {
            listener.formDownloadComplete(results);
        }
    }

    @Override
    public void taskComplete(HashMap<String, Object> results) {
        mRootButton.setEnabled(true);
        mDownloadButton.setEnabled(toDownload.size() > 0);
        mSearchButton.setEnabled(true);
        setProgressBarIndeterminateVisibility(false);

        if (results == null) {
            // if results was null, then got a google exception
            // requiring the user to authorize
            return;
        }

        String parentId = (String)results.get(PARENT_ID_KEY);
        String currentDir = (String)results.get(CURRENT_ID_KEY);

        if (MyDrive) {
            mRootButton.setText(getString(R.string.go_shared));
        } else {
            mRootButton.setText(getString(R.string.go_drive));
        }

        if (currentDir.equals(rootId) || (currentDir.equals("root"))) {
            mBackButton.setEnabled(false);
        } else {
            mBackButton.setEnabled(true);
        }
        mParentId = parentId;

        if (mCurrentPath.empty()) {
            if (MyDrive) {
                mCurrentPath.add(getString(R.string.go_drive));
            } else {
                mCurrentPath.add(getString(R.string.go_shared));
            }
        }

    }

    @Override
    protected void onPause() {
        if (mRetrieveDriveFileContentsAsyncTask != null) {
            mRetrieveDriveFileContentsAsyncTask.setTaskListener(null);
        }
        if (mGetFileTask != null) {
            mGetFileTask.setGoogleDriveFormDownloadListener(null);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRetrieveDriveFileContentsAsyncTask != null) {
            mRetrieveDriveFileContentsAsyncTask.setTaskListener(this);
        }
        if (mGetFileTask != null) {
            mGetFileTask.setGoogleDriveFormDownloadListener(this);
        }
    }

    @Override
    public void formDownloadComplete(HashMap<String, Object> results) {
        try {
            dismissDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }

        StringBuilder sb = new StringBuilder();
        Iterator<String> it = results.keySet().iterator();

        while (it.hasNext()) {
            String id = it.next();
            sb.append(id + " :: " + results.get(id) + "\n");
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

    private boolean testNetwork() {
        ConnectivityManager manager = (ConnectivityManager)this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo currentNetworkInfo = manager.getActiveNetworkInfo();
        if (currentNetworkInfo == null) {
            return false;
        }
        return (currentNetworkInfo.getState() == NetworkInfo.State.CONNECTED);
    }

    public void listFiles(String dir, String query) {
        setProgressBarIndeterminateVisibility(true);
        adapter = null;
        mRetrieveDriveFileContentsAsyncTask = new RetrieveDriveFileContentsAsyncTask();
        mRetrieveDriveFileContentsAsyncTask.setTaskListener(GoogleDriveActivity.this);
        if (query != null) {
            mRetrieveDriveFileContentsAsyncTask.execute(dir, query);
        } else {
            mRetrieveDriveFileContentsAsyncTask.execute(dir);

        }
    }

    public void listFiles(String dir) {
        listFiles(dir, null);
    }

}
