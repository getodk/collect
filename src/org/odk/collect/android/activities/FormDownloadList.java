/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.collect.android.activities;

import java.util.ArrayList;
import java.util.HashMap;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.preferences.ServerPreferences;
import org.odk.collect.android.tasks.DownloadFormsTask;
import org.odk.collect.android.utilities.FileUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


/**
 * Responsible for displaying, adding and deleting all the valid forms in the forms directory.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormDownloadList extends ListActivity implements FormDownloaderListener {
    private static final String t = "RemoveFileManageList";

    private static final int PROGRESS_DIALOG = 1;
    private static final int MENU_PREFERENCES = Menu.FIRST;

    private static final String BUNDLE_TOGGLED_KEY = "toggled";
    private static final String BUNDLE_FORM_LIST = "formlist";
    public static final String LIST_URL = "listurl";
    public static final String DL_ERROR = "dlerror";
    public static final String FILES_DOWNLOADED = "filesdownloaded";
    public static final String DIALOG_TITLE = "dialogtitle";
    public static final String DIALOG_MSG = "dialogmsg";

    private String mAlertMsg;
    private boolean mAlertShowing = false;
    private String mAlertTitle;

    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;
    private Button mActionButton;

    private DownloadFormsTask mDownloadFormsTask;
    private Button mToggleButton;
    private Button mRefreshButton;

    private HashMap<String, String> mFormNamesAndURLs;
    private ArrayAdapter<String> mFileAdapter;

    private boolean mToggled = false;

    private int totalCount;


    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_file_manage_list);

        // need white background before load
        getListView().setBackgroundColor(Color.WHITE);

        mActionButton = (Button) findViewById(R.id.add_button);
        mActionButton.setEnabled(false);
        mActionButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                downloadSelectedFiles();
                mToggled = false;
            }
        });

        mToggleButton = (Button) findViewById(R.id.toggle_button);
        mToggleButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // toggle selections of items to all or none
                ListView ls = getListView();
                mToggled = !mToggled;

                for (int pos = 0; pos < ls.getCount(); pos++)
                    ls.setItemChecked(pos, mToggled);
            }
        });

        mRefreshButton = (Button) findViewById(R.id.refresh_button);
        mRefreshButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mToggled = false;
                downloadFormList();
            }
        });


        if (savedInstanceState != null) {
            // If the screen has rotated, the hashmap with the form names and urls is passed here.
            if (savedInstanceState.containsKey(BUNDLE_FORM_LIST)) {
                mFormNamesAndURLs =
                        (HashMap<String, String>) savedInstanceState
                                .getSerializable(BUNDLE_FORM_LIST);
            }
            // indicating whether or not select-all is on or off.
            if (savedInstanceState.containsKey(BUNDLE_TOGGLED_KEY)) {
                mToggled = savedInstanceState.getBoolean(BUNDLE_TOGGLED_KEY);
            }

            // to restore alert dialog.
            //TODO: make these constants
            if (savedInstanceState.containsKey("title")) {
                mAlertTitle = savedInstanceState.getString("title");
            }
            if (savedInstanceState.containsKey("msg")) {
                mAlertMsg = savedInstanceState.getString("msg");
            }
            if (savedInstanceState.containsKey("msg")) {
                mAlertShowing = savedInstanceState.getBoolean("showing");
            }
        }

        if (mAlertShowing) {
            this.createAlertDialog(mAlertTitle, mAlertMsg);
        }

        mDownloadFormsTask = (DownloadFormsTask) getLastNonConfigurationInstance();
        if (mDownloadFormsTask == null) {
            downloadFormList();
        } else if (mDownloadFormsTask.getStatus() == AsyncTask.Status.FINISHED) {
            try {
                dismissDialog(PROGRESS_DIALOG);
            } catch (IllegalArgumentException e) {
                Log.w(t, "Attempting to close a dialog that was not previously opened");
            }
            buildView();
        }
    }


    @SuppressWarnings("unchecked")
    private void downloadFormList() {
        mFormNamesAndURLs = new HashMap<String, String>();
        if (mProgressDialog != null) {
            // This is needed because onPrepareDialog() is broken in 1.6.
            mProgressDialog.setMessage(getString(R.string.please_wait));
        }
        showDialog(PROGRESS_DIALOG);

        FileUtils.createFolder(GlobalConstants.CACHE_PATH);
        mDownloadFormsTask = new DownloadFormsTask();
        mDownloadFormsTask.setDownloaderListener(this);

        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String url =
                settings
                        .getString(ServerPreferences.KEY_SERVER, getString(R.string.default_server))
                        + "/formList";

        HashMap<String, String> arg = new HashMap<String, String>();
        arg.put(LIST_URL, url);
        mDownloadFormsTask.execute(arg);
    }


    //TODO: make these constants
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_TOGGLED_KEY, mToggled);
        outState.putSerializable(BUNDLE_FORM_LIST, mFormNamesAndURLs);
        outState.putString("title", mAlertTitle);
        outState.putString("msg", mAlertMsg);
        outState.putBoolean("showing", mAlertShowing);
    }


    private void buildView() {
        ArrayList<String> formNames = new ArrayList<String>(mFormNamesAndURLs.keySet());

        mFileAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,
                        formNames);
        setListAdapter(mFileAdapter);
        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        if (mFileAdapter.getCount() == 0) {
            mActionButton.setEnabled(false);
        } else {
            mActionButton.setEnabled(true);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_PREFERENCES, 0, getString(R.string.server_preferences)).setIcon(
                android.R.drawable.ic_menu_preferences);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                createPreferencesMenu();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    private void createPreferencesMenu() {
        Intent i = new Intent(this, ServerPreferences.class);
        startActivity(i);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mDownloadFormsTask.setDownloaderListener(null);
                            }
                        };
                mProgressDialog.setTitle(getString(R.string.downloading_data));
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
        }
        return null;
    }


    /**
     * Adds the selected form
     */
    @SuppressWarnings("unchecked")
    private void downloadSelectedFiles() {
        totalCount = 0;
        HashMap<String, String> filesToDownload = new HashMap<String, String>();

        SparseBooleanArray sba = getListView().getCheckedItemPositions();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (sba.get(i, false)) {
                String form = (String) getListAdapter().getItem(i);
                filesToDownload.put(form, mFormNamesAndURLs.get(form));
            }
        }
        totalCount = filesToDownload.size();

        if (totalCount > 0) {
            // show dialog box
            showDialog(PROGRESS_DIALOG);

            FileUtils.createFolder(GlobalConstants.FORMS_PATH);
            mDownloadFormsTask = new DownloadFormsTask();
            mDownloadFormsTask.setDownloaderListener(this);
            mDownloadFormsTask.execute(filesToDownload);
        } else {
            Toast.makeText(getApplicationContext(), R.string.noselect_error, Toast.LENGTH_SHORT)
                    .show();
        }
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        return mDownloadFormsTask;
    }


    @Override
    protected void onDestroy() {
        if (mDownloadFormsTask != null) {
            mDownloadFormsTask.setDownloaderListener(null);
        }
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        if (mDownloadFormsTask != null) {
            mDownloadFormsTask.setDownloaderListener(this);
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        super.onPause();
    }


    public void formDownloadingComplete(HashMap<String, String> result) {
        dismissDialog(PROGRESS_DIALOG);

        if (result != null && result.containsKey(FILES_DOWNLOADED)) {
            // We just downloaded a bunch of files
            String message = "";
            String title = "";
            if (result.containsKey(DIALOG_MSG)) {
                message = (String) result.get(DIALOG_MSG);
            }

            if (result.containsKey(DIALOG_TITLE)) {
                title = (String) result.get(DIALOG_TITLE);
            }

            createAlertDialog(title, message);

        } else if (result.containsKey(DL_ERROR)) {
            createAlertDialog("Error", (String) result.get(DL_ERROR));
        } else {
            // we have just downloaded the form list
            mFormNamesAndURLs = result;
        }
        buildView();
    }


    private void createAlertDialog(String title, String message) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setTitle(title);
        mAlertDialog.setMessage(message);
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // ok
                        // just close the dialog
                        mAlertShowing = false;
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), quitListener);
        mAlertShowing = true;
        mAlertMsg = message;
        mAlertTitle = title;
        mAlertDialog.show();
    }


    public void progressUpdate(String currentFile, int progress, int total) {
        mProgressDialog.setMessage("Fetching " + currentFile + ".\nFile " + progress + " of "
                + total + " item(s)...");
    }

}


// TODO: make dialog persist through screen rotations.
