/*
 * Copyright (C) 2017 University of Washington
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
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.listeners.DeleteInstancesListener;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.DeleteInstancesTask;
import org.odk.collect.android.tasks.InstanceSyncTask;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for displaying and deleting all the saved form instances
 * directory.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class DataManagerList extends InstanceListFragment
        implements DeleteInstancesListener, DiskSyncListener, View.OnClickListener {
    private static final String TAG = "DataManagerList";
    DeleteInstancesTask mDeleteInstancesTask = null;
    private AlertDialog mAlertDialog;
    private InstanceSyncTask instanceSyncTask;

    public static DataManagerList newInstance() {
        return new DataManagerList();
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

        setupAdapter(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");
        instanceSyncTask = new InstanceSyncTask();
        instanceSyncTask.setDiskSyncListener(this);
        instanceSyncTask.execute();

        super.onViewCreated(rootView, savedInstanceState);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle bundle) {
        super.onViewStateRestored(bundle);
    }

    @Override
    public void onResume() {
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
    public void onPause() {
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
        TextView textView = (TextView) rootView.findViewById(R.id.status_text);
        textView.setText(result);
    }

    @Override
    protected void setupAdapter(String sortOrder) {
        List<Long> checkedInstances = new ArrayList<>();
        for (long a : getListView().getCheckedItemIds()) {
            checkedInstances.add(a);
        }
        String[] data = new String[]{InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT};
        int[] view = new int[]{R.id.text1, R.id.text2};

        mListAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.two_item_multiple_choice, new InstancesDao().getSavedInstancesCursor(sortOrder), data, view);
        setListAdapter(mListAdapter);
        checkPreviouslyCheckedItems();
    }

    @Override
    protected void filter(CharSequence charSequence) {
        mListAdapter.changeCursor(new InstancesDao().getFilteredSavedInstancesCursor(charSequence));
        super.filter(charSequence);
    }

    /**
     * Create the instance delete dialog
     */
    private void createDeleteInstancesDialog() {
        logger.logAction(this, "createDeleteInstancesDialog",
                "show");

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
            mDeleteInstancesTask.setContentResolver(getActivity().getContentResolver());
            mDeleteInstancesTask.setDeleteListener(this);
            mDeleteInstancesTask.execute(getCheckedIdObjects());
        } else {
            ToastUtils.showLongToast(R.string.file_delete_in_progress);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long rowId) {
        super.onListItemClick(l, v, position, rowId);
    }

    @Override
    public void deleteComplete(int deletedInstances) {
        Log.i(TAG, "Delete instances complete");
        logger.logAction(this, "deleteComplete",
                Integer.toString(deletedInstances));
        final int toDeleteCount = mDeleteInstancesTask.getToDeleteCount();

        if (deletedInstances == toDeleteCount) {
            // all deletes were successful
            ToastUtils.showShortToast(getString(R.string.file_deleted_ok, String.valueOf(deletedInstances)));
        } else {
            // had some failures
            Log.e(TAG, "Failed to delete "
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_button:
                int checkedItemCount = getCheckedCount();
                logger.logAction(this, "deleteButton", Integer.toString(checkedItemCount));
                if (checkedItemCount > 0) {
                    createDeleteInstancesDialog();
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

}
