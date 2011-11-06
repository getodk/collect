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

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.tasks.DiskSyncTask;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Responsible for displaying and deleting all the valid forms in the forms directory.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormManagerList extends ListActivity implements DiskSyncListener {
    private static String t = "FormManagerList";
    private AlertDialog mAlertDialog;
    private Button mDeleteButton;

    private SimpleCursorAdapter mInstances;
    private ArrayList<Long> mSelected = new ArrayList<Long>();
    private boolean mRestored = false;
    private final String SELECTED = "selected";

    DiskSyncTask mDiskSyncTask;
    
    private final String syncMsgKey = "syncmsgkey";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_manage_list);

        mDeleteButton = (Button) findViewById(R.id.delete_button);
        mDeleteButton.setText(getString(R.string.delete_file));
        mDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelected.size() > 0) {
                    createDeleteDialog();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.noselect_error,
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

        mDiskSyncTask = (DiskSyncTask) getLastNonConfigurationInstance();
        if (mDiskSyncTask == null) {
            mDiskSyncTask = new DiskSyncTask();
            mDiskSyncTask.setDiskSyncListener(this);
            mDiskSyncTask.execute((Void[]) null);
        }

        if (mDiskSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
            //TextView tv = (TextView) findViewById(R.id.status_text);
            //tv.setText(R.string.finished_disk_scan);
            mDiskSyncTask.setDiskSyncListener(null);
        }

        Cursor c = managedQuery(FormsColumns.CONTENT_URI, null, null, null, null);

        String[] data = new String[] {
                FormsColumns.DISPLAY_NAME, FormsColumns.DISPLAY_SUBTEXT
        };
        int[] view = new int[] {
                R.id.text1, R.id.text2
        };

        // render total instance view
        mInstances =
            new SimpleCursorAdapter(this, R.layout.two_item_multiple_choice, c, data, view);
        setListAdapter(mInstances);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setItemsCanFocus(false);
        mDeleteButton.setEnabled(!(mSelected.size() == 0));

        // if current activity is being reinitialized due to changing orientation
        // restore all check marks for ones selected
        if (mRestored) {
            ListView ls = getListView();
            for (long id : mSelected) {
                for (int pos = 0; pos < ls.getCount(); pos++) {
                    if (id == ls.getItemIdAtPosition(pos)) {
                        ls.setItemChecked(pos, true);
                        break;
                    }
                }

            }
            mRestored = false;
        }
        
        if (savedInstanceState != null && savedInstanceState.containsKey(syncMsgKey)) {
            TextView tv = (TextView) findViewById(R.id.status_text);
            tv.setText(savedInstanceState.getString(syncMsgKey));
        }
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        // pass the thread on restart
        return mDiskSyncTask;
    }


    /**
     * Create the file delete dialog
     */
    private void createDeleteDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setTitle(getString(R.string.delete_file));
        mAlertDialog.setMessage(getString(R.string.delete_confirm, mSelected.size()));
        DialogInterface.OnClickListener dialogYesNoListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    switch (i) {
                        case DialogInterface.BUTTON1: // delete and
                            deleteSelectedFiles();
                            mSelected.clear();
                            getListView().clearChoices();
                            break;
                        case DialogInterface.BUTTON2: // do nothing
                            break;
                    }
                }

            };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.delete_yes), dialogYesNoListener);
        mAlertDialog.setButton2(getString(R.string.delete_no), dialogYesNoListener);
        mAlertDialog.show();
    }


    /**
     * Deletes the selected files.First from the database then from the file system
     */
    private void deleteSelectedFiles() {
        ContentResolver cr = getContentResolver();
        int deleted = 0;
        for (int i = 0; i < mSelected.size(); i++) {
            Uri deleteForm =
                Uri.withAppendedPath(FormsColumns.CONTENT_URI, mSelected.get(i).toString());
            deleted += cr.delete(deleteForm, null, null);
        }

        if (deleted == mSelected.size()) {
            // all deletes were successful
            Toast.makeText(getApplicationContext(), getString(R.string.file_deleted_ok, deleted),
                Toast.LENGTH_SHORT).show();
        } else {
            // had some failures
            Toast.makeText(
                getApplicationContext(),
                getString(R.string.file_deleted_error, mSelected.size() - deleted + " of "
                        + mSelected.size()), Toast.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // get row id from db
        Cursor c = (Cursor) getListAdapter().getItem(position);
        long k = c.getLong(c.getColumnIndex(FormsColumns._ID));

        // add/remove from selected list
        if (mSelected.contains(k))
            mSelected.remove(k);
        else
            mSelected.add(k);

        mDeleteButton.setEnabled(!(mSelected.size() == 0));

    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        long[] selectedArray = savedInstanceState.getLongArray(SELECTED);
        for (int i = 0; i < selectedArray.length; i++)
            mSelected.add(selectedArray[i]);
        mRestored = true;
        mDeleteButton.setEnabled(selectedArray.length > 0);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        long[] selectedArray = new long[mSelected.size()];
        for (int i = 0; i < mSelected.size(); i++)
            selectedArray[i] = mSelected.get(i);
        outState.putLongArray(SELECTED, selectedArray);
        TextView tv = (TextView) findViewById(R.id.status_text);
        outState.putString(syncMsgKey, tv.getText().toString());
    }
    
    
    @Override
    protected void onResume() {
        mDiskSyncTask.setDiskSyncListener(this);
        super.onResume();
    }


    @Override
    protected void onPause() {
        mDiskSyncTask.setDiskSyncListener(null);
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }

        super.onPause();
    }


    @Override
    public void SyncComplete(String result) {
        Log.i(t, "Disk scan complete");
        TextView tv = (TextView) findViewById(R.id.status_text);
        tv.setText(result);
    }
}
