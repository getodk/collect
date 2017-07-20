/*
 * Copyright (C) 2017 Smap Consulting Pty Ltd
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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainListActivity;
import org.odk.collect.android.activities.TaskTabs;
import org.odk.collect.android.adapters.TaskListArrayAdapter;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.listeners.DeleteInstancesListener;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.loaders.TaskLoader;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.DeleteInstancesTask;
import org.odk.collect.android.tasks.InstanceSyncTask;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Responsible for displaying tasks on the main fieldTask screen
 *
 */
public class TaskManagerList extends InstanceListFragment
        implements  View.OnClickListener,
        LoaderManager.LoaderCallbacks<List<TaskEntry>>{
    private static final String DATA_MANAGER_LIST_SORTING_ORDER = "dataManagerListSortingOrder";

    DeleteInstancesTask deleteInstancesTask = null;
    private AlertDialog alertDialog;
    private InstanceSyncTask instanceSyncTask;
    private static final int TASK_LOADER_ID = 1;

    private TaskListArrayAdapter mAdapter;
    private TaskTabs mActivity;

    public static TaskManagerList newInstance() {
        return new TaskManagerList();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {

        deleteButton.setOnClickListener(this);
        toggleButton.setOnClickListener(this);

        // smap initiate loader
        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);

        //setupAdapter();
        //instanceSyncTask = new InstanceSyncTask();
        //instanceSyncTask.setDiskSyncListener(this);
        //instanceSyncTask.execute();

        super.onViewCreated(rootView, savedInstanceState);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle bundle) {
        super.onViewStateRestored(bundle);
    }

    @Override
    public void onResume() {

        //if (instanceSyncTask != null) {
        //    instanceSyncTask.setDiskSyncListener(this);
        //}
        super.onResume();

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
    public Loader<List<TaskEntry>> onCreateLoader(int id, Bundle args) {
        return new TaskLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<TaskEntry>> loader, List<TaskEntry> data) {

        Timber.i(data.toString());

        // smap TODO enable when adapter added
        //mAdapter.setData(data);

        // TODO Smap
        //tabsActivity.setLocationTriggers(data, false);      // NFC and geofence triggers

        // smap todo
        //if (isResumed()) {
        //    setListShown(true);
        //} else {
        //    setListShownNoAnimation(true);
        //}
    }

    @Override
    public void onLoaderReset(Loader<List<TaskEntry>> loader) {
        mAdapter.setData(null);
    }

    //@Override
    //public void syncComplete(String result) {
    //    TextView textView = (TextView) rootView.findViewById(R.id.status_text);
    //    textView.setText(result);
    //}

    private void setupAdapter() {

        String[] data = new String[]{InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT};
        int[] view = new int[]{R.id.text1, R.id.text2};

        listAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.two_item_multiple_choice, getCursor(), data, view);
        setListAdapter(listAdapter);
        checkPreviouslyCheckedItems();
    }

    @Override
    protected String getSortingOrderKey() {
        return DATA_MANAGER_LIST_SORTING_ORDER;
    }

    @Override
    protected void updateAdapter() {
        listAdapter.changeCursor(getCursor());
        super.updateAdapter();
    }

    private Cursor getCursor() {
        return new InstancesDao().getSavedInstancesCursor(getFilterText(), getSortingOrder());
    }



    @Override
    public void onListItemClick(ListView l, View v, int position, long rowId) {
        super.onListItemClick(l, v, position, rowId);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toggle_button:
                ListView lv = getListView();
                boolean allChecked = toggleChecked(lv);
                toggleButtonLabel(toggleButton, getListView());
                deleteButton.setEnabled(allChecked);
                break;
        }
    }

}
