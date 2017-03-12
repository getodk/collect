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

package org.odk.collect.android.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.listeners.DeleteFormsListener;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.tasks.DeleteFormsTask;
import org.odk.collect.android.tasks.DiskSyncTask;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.VersionHidingCursorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for displaying and deleting all the valid forms in the forms
 * directory.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormManagerList extends FormListFragment implements DiskSyncListener,
        DeleteFormsListener, View.OnClickListener {
    private static final String syncMsgKey = "syncmsgkey";
    private static String TAG = "FormManagerList";
    BackgroundTasks mBackgroundTasks; // handed across orientation changes
    private AlertDialog mAlertDialog;

    public static FormManagerList newInstance() {
        return new FormManagerList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {

        mDeleteButton.setOnClickListener(this);
        mToggleButton.setOnClickListener(this);

        setupAdapter(FormsColumns.DISPLAY_NAME + " ASC, " + FormsColumns.JR_VERSION + " DESC");

        if (mBackgroundTasks == null) {
            mBackgroundTasks = new BackgroundTasks();
            mBackgroundTasks.mDiskSyncTask = new DiskSyncTask();
            mBackgroundTasks.mDiskSyncTask.setDiskSyncListener(this);
            mBackgroundTasks.mDiskSyncTask.execute((Void[]) null);
        }
        super.onViewCreated(rootView, savedInstanceState);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onResume() {
        // hook up to receive completion events
        mBackgroundTasks.mDiskSyncTask.setDiskSyncListener(this);
        if (mBackgroundTasks.mDeleteFormsTask != null) {
            mBackgroundTasks.mDeleteFormsTask.setDeleteListener(this);
        }
        super.onResume();
        // async task may have completed while we were reorienting...
        if (mBackgroundTasks.mDiskSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
            syncComplete(mBackgroundTasks.mDiskSyncTask.getStatusMessage());
        }
        if (mBackgroundTasks.mDeleteFormsTask != null
                && mBackgroundTasks.mDeleteFormsTask.getStatus() == AsyncTask.Status.FINISHED) {
            deleteComplete(mBackgroundTasks.mDeleteFormsTask.getDeleteCount());
        }
    }

    @Override
    public void onPause() {
        mBackgroundTasks.mDiskSyncTask.setDiskSyncListener(null);
        if (mBackgroundTasks.mDeleteFormsTask != null) {
            mBackgroundTasks.mDeleteFormsTask.setDeleteListener(null);
        }
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }

        super.onPause();
    }

    @Override
    protected void setupAdapter(String sortOrder) {
        List<Long> checkedForms = new ArrayList<>();
        for (long a : getListView().getCheckedItemIds()) {
            checkedForms.add(a);
        }
        Cursor c = new FormsDao().getFormsCursor(sortOrder);
        String[] data = new String[]{FormsColumns.DISPLAY_NAME, FormsColumns.DISPLAY_SUBTEXT, FormsColumns.JR_VERSION};
        int[] view = new int[]{R.id.text1, R.id.text2, R.id.text3};

        // render total instance view
        SimpleCursorAdapter cursorAdapter = new VersionHidingCursorAdapter(
                FormsColumns.JR_VERSION, getActivity(),
                R.layout.two_item_multiple_choice, c, data, view);
        setListAdapter(cursorAdapter);
        checkPreviouslyCheckedItems(checkedForms, c);
    }

    /**
     * Create the form delete dialog
     */
    private void createDeleteFormsDialog() {
        logger.logAction(this, "createDeleteFormsDialog", "show");
        mAlertDialog = new AlertDialog.Builder(getContext()).create();
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
                    .setContentResolver(getActivity().getContentResolver());
            mBackgroundTasks.mDeleteFormsTask.setDeleteListener(this);
            mBackgroundTasks.mDeleteFormsTask.execute(getCheckedIdObjects());
        } else {
            ToastUtils.showLongToast(R.string.file_delete_in_progress);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long rowId) {
        super.onListItemClick(l, v, position, rowId);
    }

    @Override
    public void syncComplete(String result) {
        Log.i(TAG, "Disk scan complete");
        TextView tv = (TextView) rootView.findViewById(R.id.status_text);
        tv.setText(result);
    }

    @Override
    public void deleteComplete(int deletedForms) {
        Log.i(TAG, "Delete forms complete");
        logger.logAction(this, "deleteComplete", Integer.toString(deletedForms));
        final int toDeleteCount = mBackgroundTasks.mDeleteFormsTask.getToDeleteCount();

        if (deletedForms == toDeleteCount) {
            // all deletes were successful
            ToastUtils.showShortToast(getString(R.string.file_deleted_ok, String.valueOf(deletedForms)));
        } else {
            // had some failures
            Log.e(TAG, "Failed to delete " + (toDeleteCount - deletedForms) + " forms");
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_button:
                logger.logAction(this, "deleteButton", Integer.toString(getCheckedCount()));

                if (areCheckedItems()) {
                    createDeleteFormsDialog();
                } else {
                    ToastUtils.showShortToast(R.string.noselect_error);
                }
                break;

            case R.id.toggle_button:
                ListView lv = getListView();
                boolean allChecked = toggleChecked(lv);
                toggleButtonLabel(mToggleButton, getListView());
                mDeleteButton.setEnabled(allChecked);
                break;
        }
    }

    private static class BackgroundTasks {
        DiskSyncTask mDiskSyncTask = null;
        DeleteFormsTask mDeleteFormsTask = null;

        BackgroundTasks() {
        }
    }
}
