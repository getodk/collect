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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.prefs.GlobalPreferences;
import org.odk.collect.android.tasks.FormDownloadTask;
import org.odk.collect.android.utils.FileUtils;
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

public class RemoteFileManager extends ListActivity implements FormDownloaderListener {

    private static final int PROGRESS_DIALOG = 1;
    private static final int MENU_ADD = Menu.FIRST;

    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;

    private FormDownloadTask mFormDownloadTask;

    private boolean mLoadingList;
    private int mAddPosition;

    private String mFormList = GlobalConstants.CACHE_PATH + "formlist.xml";

    private ArrayList<String> mFormName = new ArrayList<String>();
    private ArrayList<String> mFormUrl = new ArrayList<String>();
    private ArrayAdapter<String> mFileAdapter;


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
        mFormDownloadTask.setDownloaderListener(RemoteFileManager.this);

        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String url =
                settings
                        .getString(GlobalPreferences.KEY_SERVER, getString(R.string.default_server))
                        + "/formList";
        mFormDownloadTask.execute(url, mFormList);

    }


    private void buildView() {

        // create xml document
        File file = new File(mFormList);
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
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,
                            mFormName);
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
                if (getListView().getCheckedItemPosition() != -1) {
                    addSelectedForm();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.noselect_error,
                            Toast.LENGTH_SHORT).show();
                }
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
                                //finish();
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
    private void addSelectedForm() {

        // show dialog box
        showDialog(PROGRESS_DIALOG);

        // position of the form
        mAddPosition = getListView().getCheckedItemPosition();

        mLoadingList = false;
        FileUtils.createFolder(GlobalConstants.FORMS_PATH);
        mFormDownloadTask = new FormDownloadTask();
        mFormDownloadTask.setDownloaderListener(RemoteFileManager.this);
        mFormDownloadTask.execute(mFormUrl.get(mAddPosition), GlobalConstants.FORMS_PATH
                + mFormName.get(mAddPosition));

    }


    public void downloadingComplete(Boolean result, String name) {

        // no need for dialog
        dismissDialog(PROGRESS_DIALOG);

        // show message only if successful and not loading formlist'
        if (!result) {
            Toast.makeText(this, getString(R.string.download_fail, name), Toast.LENGTH_LONG).show();
        } else {

            if (mLoadingList) {
                buildView();
            } else {
                // clean up choices
                Toast.makeText(this, getString(R.string.download_successful, name),
                        Toast.LENGTH_SHORT).show();
                mFileAdapter.notifyDataSetChanged();
                getListView().clearChoices();
                
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);
        cb.setChecked(!cb.isChecked());

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


}
