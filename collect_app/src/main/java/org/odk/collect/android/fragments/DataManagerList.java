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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.listeners.DeleteInstancesListener;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.DeleteInstancesTask;
import org.odk.collect.android.tasks.InstanceSyncTask;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Responsible for displaying and deleting all the saved form instances
 * directory.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class DataManagerList extends InstanceListFragment
        implements DeleteInstancesListener, DiskSyncListener, View.OnClickListener {
    private static final String DATA_MANAGER_LIST_SORTING_ORDER = "dataManagerListSortingOrder";

    DeleteInstancesTask deleteInstancesTask;
    private AlertDialog alertDialog;
    private InstanceSyncTask instanceSyncTask;
    private ProgressDialog progressDialog;
    @Inject
    SmsSubmissionManagerContract smsSubmissionManager;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Collect.getInstance().getComponent().inject(this);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, Bundle savedInstanceState) {

        deleteButton.setOnClickListener(this);
        toggleButton.setOnClickListener(this);

        setupAdapter();
        instanceSyncTask = new InstanceSyncTask();
        instanceSyncTask.setDiskSyncListener(this);
        instanceSyncTask.execute();

        super.onViewCreated(rootView, savedInstanceState);
    }

    @Override
    public void onResume() {
        // hook up to receive completion events
        if (deleteInstancesTask != null) {
            deleteInstancesTask.setDeleteListener(this);
        }
        if (instanceSyncTask != null) {
            instanceSyncTask.setDiskSyncListener(this);
        }
        super.onResume();
        // async task may have completed while we were reorienting...
        if (deleteInstancesTask != null
                && deleteInstancesTask.getStatus() == AsyncTask.Status.FINISHED) {
            deleteComplete(deleteInstancesTask.getDeleteCount());
        }
    }

    @Override
    public void onPause() {
        if (deleteInstancesTask != null) {
            deleteInstancesTask.setDeleteListener(null);
        }
        if (instanceSyncTask != null) {
            instanceSyncTask.setDiskSyncListener(null);
        }
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public void syncComplete(@NonNull String result) {
        Timber.i("Disk scan complete");
        hideProgressBarAndAllow();
        showSnackbar(result);
    }

    private void setupAdapter() {
        String[] data = new String[]{InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT};
        int[] view = new int[]{R.id.text1, R.id.text2};

        listAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.two_item_multiple_choice, null, data, view);
        setListAdapter(listAdapter);
        checkPreviouslyCheckedItems();
    }

    @Override
    protected String getSortingOrderKey() {
        return DATA_MANAGER_LIST_SORTING_ORDER;
    }

    @Override
    protected CursorLoader getCursorLoader() {
        return new InstancesDao().getSavedInstancesCursorLoader(getFilterText(), getSortingOrder());
    }

    /**
     * Create the instance delete dialog
     */
    private void createDeleteInstancesDialog() {
        logger.logAction(this, "createDeleteInstancesDialog",
                "show");

        alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(getString(R.string.delete_file));
        alertDialog.setMessage(getString(R.string.delete_confirm,
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
                                    toggleButton.setEnabled(false);
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE: // do nothing
                                logger.logAction(this,
                                        "createDeleteInstancesDialog", "cancel");
                                break;
                        }
                    }
                };
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.delete_yes),
                dialogYesNoListener);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.delete_no),
                dialogYesNoListener);
        alertDialog.show();
    }

    @Override
    public void progressUpdate(int progress, int total) {
        String message = String.format(getResources().getString(R.string.deleting_form_dialog_update_message), progress, total);
        progressDialog.setMessage(message);
    }

    /**
     * Deletes the selected files. Content provider handles removing the files
     * from the filesystem.
     */
    private void deleteSelectedInstances() {
        if (deleteInstancesTask == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getResources().getString(R.string.form_delete_message));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();

            deleteSmsSubmissions(getCheckedIdObjects());

            deleteInstancesTask = new DeleteInstancesTask();
            deleteInstancesTask.setContentResolver(getActivity().getContentResolver());
            deleteInstancesTask.setDeleteListener(this);
            deleteInstancesTask.execute(getCheckedIdObjects());
        } else {
            ToastUtils.showLongToast(R.string.file_delete_in_progress);
        }
    }

    private void deleteSmsSubmissions(Long[] ids) {
        List<Long> list = Arrays.asList(ids);

        for (Long id : list) {
            smsSubmissionManager.forgetSubmission(String.valueOf(id));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long rowId) {
        super.onListItemClick(l, v, position, rowId);
    }

    @Override
    public void deleteComplete(int deletedInstances) {
        Timber.i("Delete instances complete");
        logger.logAction(this, "deleteComplete",
                Integer.toString(deletedInstances));
        final int toDeleteCount = deleteInstancesTask.getToDeleteCount();

        if (deletedInstances == toDeleteCount) {
            // all deletes were successful
            ToastUtils.showShortToast(getString(R.string.file_deleted_ok, String.valueOf(deletedInstances)));
        } else {
            // had some failures
            Timber.e("Failed to delete %d instances", (toDeleteCount - deletedInstances));
            ToastUtils.showLongToast(getString(R.string.file_deleted_error,
                    String.valueOf(toDeleteCount - deletedInstances),
                    String.valueOf(toDeleteCount)));
        }

        deleteInstancesTask = null;
        getListView().clearChoices(); // doesn't unset the checkboxes
        for (int i = 0; i < getListView().getCount(); ++i) {
            getListView().setItemChecked(i, false);
        }
        deleteButton.setEnabled(false);

        progressDialog.dismiss();
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
                if (allChecked) {
                    for (int i = 0; i < lv.getCount(); i++) {
                        selectedInstances.add(lv.getItemIdAtPosition(i));
                    }
                } else {
                    selectedInstances.clear();
                }
                toggleButtonLabel(toggleButton, getListView());
                deleteButton.setEnabled(allChecked);
                break;
        }
    }

}
