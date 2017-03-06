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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.listeners.DeleteFormsListener;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.tasks.DeleteFormsTask;
import org.odk.collect.android.tasks.DiskSyncTask;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.VersionHidingCursorAdapter;

/**
 * Responsible for displaying and deleting all the valid forms in the forms
 * directory.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormManagerList extends AppListActivity implements DiskSyncListener,
        DeleteFormsListener {
    private static String t = "FormManagerList";
    private static final String syncMsgKey = "syncmsgkey";

    private AlertDialog mAlertDialog;
    private Button mDeleteButton;
    private Button mToggleButton;

    static class BackgroundTasks {
        DiskSyncTask mDiskSyncTask = null;
        DeleteFormsTask mDeleteFormsTask = null;

        BackgroundTasks() {
        }

        ;
    }

    BackgroundTasks mBackgroundTasks; // handed across orientation changes

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(t, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_manage_list);

        mDeleteButton = (Button) findViewById(R.id.delete_button);
        mDeleteButton.setText(getString(R.string.delete_file));
        mDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.logAction(this, "deleteButton", Integer.toString(getCheckedCount()));

                if (areCheckedItems()) {
                    createDeleteFormsDialog();
                } else {
                    ToastUtils.showShortToast(R.string.noselect_error);
                }
            }
        });

        mToggleButton = (Button) findViewById(R.id.toggle_button);
        mToggleButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListView lv = getListView();
                    boolean allChecked = toggleChecked(lv);
                    toggleButtonLabel(mToggleButton, getListView());
                    mDeleteButton.setEnabled(allChecked);
                }
        });

        String sortOrder = FormsColumns.DISPLAY_NAME + " ASC, " + FormsColumns.JR_VERSION + " DESC";
        Cursor c = new FormsDao().getFormsCursor(sortOrder);

        String[] data = new String[]{FormsColumns.DISPLAY_NAME,
                FormsColumns.DISPLAY_SUBTEXT, FormsColumns.JR_VERSION};
        int[] view = new int[]{R.id.text1, R.id.text2, R.id.text3};

        // render total instance view
        SimpleCursorAdapter cursorAdapter = new VersionHidingCursorAdapter(FormsColumns.JR_VERSION, this,
                R.layout.two_item_multiple_choice, c, data, view);
        setListAdapter(cursorAdapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setItemsCanFocus(false);
        mDeleteButton.setEnabled(false);

        if (getListView().getCount() == 0) {
            mToggleButton.setEnabled(false);
        }

        if (savedInstanceState != null
                && savedInstanceState.containsKey(syncMsgKey)) {
            TextView tv = (TextView) findViewById(R.id.status_text);
            tv.setText(savedInstanceState.getString(syncMsgKey));
        }

        mBackgroundTasks = (BackgroundTasks) getLastNonConfigurationInstance();
        if (mBackgroundTasks == null) {
            mBackgroundTasks = new BackgroundTasks();
            mBackgroundTasks.mDiskSyncTask = new DiskSyncTask();
            mBackgroundTasks.mDiskSyncTask.setDiskSyncListener(this);
            mBackgroundTasks.mDiskSyncTask.execute((Void[]) null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        logger.logOnStart(this);
    }

    @Override
    protected void onStop() {
        logger.logOnStop(this);
        super.onStop();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        // pass the tasks on restart
        return mBackgroundTasks;
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        mDeleteButton.setEnabled(areCheckedItems());
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        TextView tv = (TextView) findViewById(R.id.status_text);
        bundle.putString(syncMsgKey, tv.getText().toString());
    }

    @Override
    protected void onResume() {
        // hook up to receive completion events
        mBackgroundTasks.mDiskSyncTask.setDiskSyncListener(this);
        if (mBackgroundTasks.mDeleteFormsTask != null) {
            mBackgroundTasks.mDeleteFormsTask.setDeleteListener(this);
        }
        super.onResume();
        // async task may have completed while we were reorienting...
        if (mBackgroundTasks.mDiskSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
            SyncComplete(mBackgroundTasks.mDiskSyncTask.getStatusMessage());
        }
        if (mBackgroundTasks.mDeleteFormsTask != null
                && mBackgroundTasks.mDeleteFormsTask.getStatus() == AsyncTask.Status.FINISHED) {
            deleteComplete(mBackgroundTasks.mDeleteFormsTask.getDeleteCount());
        }
    }

    @Override
    protected void onPause() {
        mBackgroundTasks.mDiskSyncTask.setDiskSyncListener(null);
        if (mBackgroundTasks.mDeleteFormsTask != null) {
            mBackgroundTasks.mDeleteFormsTask.setDeleteListener(null);
        }
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }

        super.onPause();
    }

    /**
     * Create the form delete dialog
     */
    private void createDeleteFormsDialog() {
        logger.logAction(this, "createDeleteFormsDialog", "show");
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setTitle(getString(R.string.delete_file));
        mAlertDialog.setMessage(getString(R.string.delete_confirm,
                String.valueOf(getCheckedCount())));
        DialogInterface.OnClickListener dialogYesNoListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON_POSITIVE: // delete
                                logger.logAction(this, "createDeleteFormsDialog", "delete");
                                deleteSelectedForms();
                                if (getListView().getCount() == getCheckedCount()) {
                                    mToggleButton.setEnabled(false);
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE: // do nothing
                                logger.logAction(this, "createDeleteFormsDialog", "cancel");
                                break;
                        }
                    }
                };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.delete_yes),
                dialogYesNoListener);
        mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.delete_no),
                dialogYesNoListener);
        mAlertDialog.show();
    }

    /**
     * Deletes the selected files.First from the database then from the file
     * system
     */
    private void deleteSelectedForms() {
        // only start if no other task is running
        if (mBackgroundTasks.mDeleteFormsTask == null) {
            mBackgroundTasks.mDeleteFormsTask = new DeleteFormsTask();
            mBackgroundTasks.mDeleteFormsTask
                    .setContentResolver(getContentResolver());
            mBackgroundTasks.mDeleteFormsTask.setDeleteListener(this);
            mBackgroundTasks.mDeleteFormsTask.execute(getCheckedIdObjects());
        } else {
            ToastUtils.showLongToast(R.string.file_delete_in_progress);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long rowId) {
        super.onListItemClick(l, v, position, rowId);
        logger.logAction(this, "onListItemClick", Long.toString(rowId));
        toggleButtonLabel(mToggleButton, getListView());
        mDeleteButton.setEnabled(areCheckedItems());
    }

    @Override
    public void SyncComplete(String result) {
        Log.i(t, "Disk scan complete");
        TextView tv = (TextView) findViewById(R.id.status_text);
        tv.setText(result);
    }

    @Override
    public void deleteComplete(int deletedForms) {
        Log.i(t, "Delete forms complete");
        logger.logAction(this, "deleteComplete", Integer.toString(deletedForms));
        if (deletedForms == getCheckedCount()) {
            // all deletes were successful
            ToastUtils.showShortToast(getString(R.string.file_deleted_ok, String.valueOf(deletedForms)));
        } else {
            // had some failures
            Log.e(t, "Failed to delete " + (getCheckedCount() - deletedForms) + " forms");
            ToastUtils.showLongToast(getString(R.string.file_deleted_error, String.valueOf(getCheckedCount()
                            - deletedForms), String.valueOf(getCheckedCount())));
        }
        mBackgroundTasks.mDeleteFormsTask = null;
        getListView().clearChoices(); // doesn't unset the checkboxes
        for (int i = 0; i < getListView().getCount(); ++i) {
            getListView().setItemChecked(i, false);
        }
        mDeleteButton.setEnabled(false);
    }
}
