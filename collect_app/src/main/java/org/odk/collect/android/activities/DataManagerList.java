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
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.listeners.DeleteInstancesListener;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.DeleteInstancesTask;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.tasks.InstanceSyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for displaying and deleting all the saved form instances
 * directory.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class DataManagerList extends InstanceListActivity
        implements DeleteInstancesListener, DiskSyncListener {
    private static final String t = "DataManagerList";
    private AlertDialog mAlertDialog;
    private Button mDeleteButton;
    private Button mToggleButton;

    DeleteInstancesTask mDeleteInstancesTask = null;
    private InstanceSyncTask instanceSyncTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_manage_list);

        mDeleteButton = (Button) findViewById(R.id.delete_button);
        mDeleteButton.setText(getString(R.string.delete_file));
        mDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkedItemCount = getCheckedCount();
                logger.logAction(this, "deleteButton", Integer.toString(checkedItemCount));
                if (checkedItemCount > 0) {
                    createDeleteInstancesDialog();
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

        setupAdapter(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");
        
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setItemsCanFocus(false);
        mDeleteButton.setEnabled(false);

        mDeleteInstancesTask = (DeleteInstancesTask) getLastNonConfigurationInstance();

        if (getListView().getCount() == 0) {
            mToggleButton.setEnabled(false);
        }

        instanceSyncTask = new InstanceSyncTask();
        instanceSyncTask.setDiskSyncListener(this);
        instanceSyncTask.execute();

        mSortingOptions = new String[]{
                getString(R.string.sort_by_name_asc), getString(R.string.sort_by_name_desc),
                getString(R.string.sort_by_date_asc), getString(R.string.sort_by_date_desc)
        };
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
        // pass the tasks on orientation-change restart
        return mDeleteInstancesTask;
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        Log.d(t, "onRestoreInstanceState");
        super.onRestoreInstanceState(bundle);
        mDeleteButton.setEnabled(areCheckedItems());
    }

    @Override
    protected void onResume() {
        // hook up to receive completion events
        if (mDeleteInstancesTask != null) {
            mDeleteInstancesTask.setDeleteListener(this);
        }
        if (instanceSyncTask != null) {
            instanceSyncTask.setDiskSyncListener(this);
        }
        super.onResume();
        // async task may have completed while we were reorienting...
        if (mDeleteInstancesTask != null
                && mDeleteInstancesTask.getStatus() == AsyncTask.Status.FINISHED) {
            deleteComplete(mDeleteInstancesTask.getDeleteCount());
        }
    }

    @Override
    protected void onPause() {
        if (mDeleteInstancesTask != null) {
            mDeleteInstancesTask.setDeleteListener(null);
        }
        if (instanceSyncTask != null) {
            instanceSyncTask.setDiskSyncListener(null);
        }
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public void syncComplete(String result) {
        TextView textView = (TextView) findViewById(R.id.status_text);
        textView.setText(result);
    }

    @Override
    protected void setupAdapter(String sortOrder) {
        List<Long> checkedInstances = new ArrayList();
        for (long a : getListView().getCheckedItemIds()) {
            checkedInstances.add(a);
        }
        String[] data = new String[]{InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT};
        int[] view = new int[]{R.id.text1, R.id.text2};

        Cursor cursor = new InstancesDao().getSavedInstancesCursor(sortOrder);
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,
                R.layout.two_item_multiple_choice, cursor, data, view);
        setListAdapter(cursorAdapter);
        checkPreviouslyCheckedItems(checkedInstances, cursor);
    }

    /**
     * Create the instance delete dialog
     */
    private void createDeleteInstancesDialog() {
        logger.logAction(this, "createDeleteInstancesDialog",
                "show");

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
                                logger.logAction(this,
                                        "createDeleteInstancesDialog", "delete");
                                deleteSelectedInstances();
                                if (getListView().getCount() == getCheckedCount()) {
                                    mToggleButton.setEnabled(false);
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE: // do nothing
                                logger.logAction(this,
                                        "createDeleteInstancesDialog", "cancel");
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
     * Deletes the selected files. Content provider handles removing the files
     * from the filesystem.
     */
    private void deleteSelectedInstances() {
        if (mDeleteInstancesTask == null) {
            mDeleteInstancesTask = new DeleteInstancesTask();
            mDeleteInstancesTask.setContentResolver(getContentResolver());
            mDeleteInstancesTask.setDeleteListener(this);
            mDeleteInstancesTask.execute(getCheckedIdObjects());
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
    public void deleteComplete(int deletedInstances) {
        Log.i(t, "Delete instances complete");
        logger.logAction(this, "deleteComplete",
                Integer.toString(deletedInstances));
        final int toDeleteCount = mDeleteInstancesTask.getToDeleteCount();

        if (deletedInstances == toDeleteCount) {
            // all deletes were successful
            ToastUtils.showShortToast(getString(R.string.file_deleted_ok, String.valueOf(deletedInstances)));
        } else {
            // had some failures
            Log.e(t, "Failed to delete "
                    + (toDeleteCount - deletedInstances) + " instances");
            ToastUtils.showLongToast(getString(R.string.file_deleted_error,
                            String.valueOf(toDeleteCount - deletedInstances),
                            String.valueOf(toDeleteCount)));
        }
        mDeleteInstancesTask = null;
        getListView().clearChoices(); // doesn't unset the checkboxes
        for (int i = 0; i < getListView().getCount(); ++i) {
            getListView().setItemChecked(i, false);
        }
        mDeleteButton.setEnabled(false);
    }
}
