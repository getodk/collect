/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.preferences.GlobalPreferences;
import org.odk.collect.android.tasks.FormDownloadTask;
import org.odk.collect.android.utilities.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Responsible for displaying, adding and deleting all the valid forms in the
 * forms directory.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class RemoteFileManagerList extends ListActivity implements FormDownloaderListener {

    private static final int PROGRESS_DIALOG = 1;
    private static final int MENU_ADD = Menu.FIRST;

    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;

    private FormDownloadTask mFormDownloadTask;

    private boolean mLoadingList;

    private ArrayList<String> mFormName = new ArrayList<String>();
    private ArrayList<String> mFormUrl = new ArrayList<String>();
    private ArrayAdapter<String> mFileAdapter;

    private int totalCount;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupView();
    }


    private void setupView() {

        // need white background before load
        getListView().setBackgroundColor(Color.WHITE);

        // check for existing dialog
        mFormDownloadTask = (FormDownloadTask) getLastNonConfigurationInstance();
        if (mFormDownloadTask != null && mFormDownloadTask.getStatus() == AsyncTask.Status.FINISHED) {
            try {
                dismissDialog(PROGRESS_DIALOG);
            } catch (IllegalArgumentException e) {
            }
        }

        // display dialog for form list download
        showDialog(PROGRESS_DIALOG);

        // download form list
        mLoadingList = true;
        FileUtils.createFolder(GlobalConstants.CACHE_PATH);
        mFormDownloadTask = new FormDownloadTask();
        mFormDownloadTask.setDownloaderListener(RemoteFileManagerList.this);

        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String url =
                settings
                        .getString(GlobalPreferences.KEY_SERVER, getString(R.string.default_server))
                        + "/formList";
        mFormDownloadTask.setDownloadServer(url);
        mFormDownloadTask.execute();

    }


    private void buildView() {

        // create xml document
        File file = new File(GlobalConstants.CACHE_PATH + mFormDownloadTask.formList);
        if (file.exists()) {

            Document doc = null;
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // populate arrays with form names and urls
            int formCount = 0;
            if (doc != null) {
                NodeList formElements = doc.getElementsByTagName("form");
                formCount = formElements.getLength();
                for (int i = 0; i < formCount; i++) {
                    Node n = formElements.item(i);
                    mFormName.add(n.getChildNodes().item(0).getNodeValue() + ".xml");
                    mFormUrl.add(n.getAttributes().item(0).getNodeValue());
                }
            }
            // create file adapter and create view
            mFileAdapter =
                    new ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_multiple_choice, mFormName);
            // view options
            if (mFormName.size() > 0) {
                getListView().setItemsCanFocus(false);
                getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                setListAdapter(mFileAdapter);
            } else {
                setContentView(R.layout.list_view_empty);
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ADD, 0, getString(R.string.add_file)).setIcon(
                android.R.drawable.ic_menu_add);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                downloadSelectedFiles();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
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
                                mFormDownloadTask.setDownloaderListener(null);
                                // finish();
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
    private void downloadSelectedFiles() {

        totalCount = 0;
        ArrayList<String> files = new ArrayList<String>();

        SparseBooleanArray sba = getListView().getCheckedItemPositions();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (sba.get(i, false)) {
                files.add(mFormUrl.get(i));
                files.add(mFormName.get(i));
            }
        }
        totalCount = files.size();

        if (totalCount > 0) {

            // show dialog box
            showDialog(PROGRESS_DIALOG);

            mLoadingList = false;
            FileUtils.createFolder(GlobalConstants.FORMS_PATH);
            mFormDownloadTask = new FormDownloadTask();
            mFormDownloadTask.setDownloaderListener(RemoteFileManagerList.this);
            mFormDownloadTask.execute(files.toArray(new String[totalCount]));
        } else {
            Toast.makeText(getApplicationContext(), R.string.noselect_error, Toast.LENGTH_SHORT)
                    .show();
        }
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        return mFormDownloadTask;
    }


    @Override
    protected void onDestroy() {
        if (mFormDownloadTask != null) {
            mFormDownloadTask.setDownloaderListener(null);
        }
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        if (mFormDownloadTask != null) {
            mFormDownloadTask.setDownloaderListener(this);
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


    public void downloadingComplete(ArrayList<String> result) {

        int resultSize = 0;
        dismissDialog(PROGRESS_DIALOG);

        if (result == null) {
            Toast.makeText(this, getString(R.string.load_remote_form_error), Toast.LENGTH_LONG)
                    .show();
        } else {
            if (mLoadingList) {
                buildView();
            } else {
                resultSize = result.size();
                if (resultSize == totalCount / 2) {
                    Toast.makeText(this,
                            getString(R.string.download_all_successful, totalCount / 2),
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String s = totalCount / 2 - resultSize + " of " + totalCount / 2;
                    Toast.makeText(this, getString(R.string.download_some_failed, s),
                            Toast.LENGTH_LONG).show();
                }
                int i;
                for (String url : result) {
                    i = mFormUrl.indexOf(url);
                    if (i > -1) {
                        mFormUrl.remove(i);
                        mFormName.remove(i);
                    }
                }
                
                mFileAdapter.notifyDataSetChanged();
                getListView().clearChoices();

            }

        }
    }


    public void progressUpdate(int progress, int total) {
        mProgressDialog.setMax(total);
        mProgressDialog.setProgress(progress);
    }


}
