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

package org.odk.collect.android;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Responsible for displaying, adding and deleting all the valid forms in the
 * forms directory.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormManager extends ListActivity implements FormDownloaderListener {

    private final String t = "Form Manager";

    //private static final int DIALOG_ADD_FORM = 0;
    //private static final int DIALOG_DELETE_FORM = 1;
    private static final int PROGRESS_DIALOG = 2;

    // add or delete form
    private static final int MENU_ADD = Menu.FIRST;
    private static final int MENU_DELETE = Menu.FIRST + 1;

    private String mDeleteForm;
    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;

    private ArrayList<String> mFileList;

    private FormDownloadTask mFormDownloadTask;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(t, "called onCreate");
        
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.manage_forms));


        mFormDownloadTask = (FormDownloadTask) getLastNonConfigurationInstance();
        if (mFormDownloadTask != null && mFormDownloadTask.getStatus() == AsyncTask.Status.FINISHED)
            try {
                dismissDialog(PROGRESS_DIALOG);
            } catch (IllegalArgumentException e) {
                Log.e(t, "dialog wasn't previously shown.  totally fine");
            }

        refresh();
    }


    private void refresh() {
        mFileList = FileUtils.getFilesAsArrayList(SharedConstants.FORMS_PATH);
        ArrayAdapter<String> fileAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,
                        mFileList);
        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setListAdapter(fileAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ADD, 0, getString(R.string.add_form)).setIcon(
                android.R.drawable.ic_menu_add);
        menu.add(0, MENU_DELETE, 0, getString(R.string.delete_form)).setIcon(
                android.R.drawable.ic_menu_delete);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                createAddDialog();
                return true;
            case MENU_DELETE:
                if (getListView().getCheckedItemPosition() != -1) {
                    createDeleteDialog();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.delete_error),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }



    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateDialog(int)
     */
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
                                finish();
                            }
                        };
                mProgressDialog.setMessage(getString(R.string.loading_form));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
        }
        return null;
    }


    /**
     * Create the form add dialog
     */
    private void createAddDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        final View v = li.inflate(R.layout.add_form, null);
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setTitle(getString(R.string.add_form));
        mAlertDialog.setView(v);
        DialogInterface.OnClickListener DialogUrl = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // ok, download form
                        EditText et = (EditText) v.findViewById(R.id.add_url);
                        showDialog(PROGRESS_DIALOG);
                        mFormDownloadTask = new FormDownloadTask();
                        mFormDownloadTask.setDownloaderListener(FormManager.this);
                        mFormDownloadTask.execute(et.getText().toString());
                        break;
                    case DialogInterface.BUTTON2: // cancel, do nothing
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), DialogUrl);
        mAlertDialog.setButton2(getString(R.string.cancel), DialogUrl);
        mAlertDialog.show();
    }


    /**
     * Create the form delete dialog
     */
    private void createDeleteDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mDeleteForm = mFileList.get(getListView().getCheckedItemPosition());
        mAlertDialog.setMessage(getString(R.string.delete_confirm, mDeleteForm));
        DialogInterface.OnClickListener dialogYesNoListener =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON1: // yes, delete
                                deleteSelectedForm();
                                refresh();

                                break;
                            case DialogInterface.BUTTON2: // no, do nothing
                                break;
                        }
                    }
                };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.yes), dialogYesNoListener);
        mAlertDialog.setButton2(getString(R.string.no), dialogYesNoListener);
        mAlertDialog.show();
    }


    /**
     * Deletes the selected form
     */
    private void deleteSelectedForm() {
        boolean deleted = FileUtils.deleteFile(SharedConstants.FORMS_PATH + "/" + mDeleteForm);
        if (deleted) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.form_deleted_ok, mDeleteForm), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.form_deleted_error, mDeleteForm), Toast.LENGTH_SHORT).show();
        }
    }


    public void downloadingComplete(Boolean result) {
        Log.e("carl", "finished downloading with result " + result);
        dismissDialog(PROGRESS_DIALOG);
        if (result) {
            Toast.makeText(this, "Download Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Download Failed", Toast.LENGTH_LONG).show();
        }
        refresh();
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        return mFormDownloadTask;
    }


    @Override
    protected void onDestroy() {
        if (mFormDownloadTask != null) mFormDownloadTask.setDownloaderListener(null);
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        if (mFormDownloadTask != null) mFormDownloadTask.setDownloaderListener(this);
        super.onResume();
    }


    @Override
    protected void onPause() {
        if (mAlertDialog != null && mAlertDialog.isShowing())
            mAlertDialog.dismiss();
        super.onPause();
    }



}
