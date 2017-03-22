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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.receivers.NetworkReceiver;
import org.odk.collect.android.tasks.InstanceSyncTask;
import org.odk.collect.android.utilities.PlayServicesUtil;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenuActivity}.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class InstanceUploaderList extends InstanceListActivity
        implements OnLongClickListener, DiskSyncListener {
    private static final String t = "InstanceUploaderList";
    private static final String SHOW_ALL_MODE = "showAllMode";

    private static final int MENU_PREFERENCES = AppListActivity.MENU_FILTER + 1;
    private static final int MENU_SHOW_UNSENT = MENU_PREFERENCES + 1;

    private static final int INSTANCE_UPLOADER = 0;

    private Button mUploadButton;

    private InstancesDao mInstanceDao;

    private InstanceSyncTask instanceSyncTask;

    private boolean mShowAllMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(t, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instance_uploader_list);

        if (savedInstanceState != null) {
            mShowAllMode = savedInstanceState.getBoolean(SHOW_ALL_MODE);
        }

        mInstanceDao = new InstancesDao();

        mUploadButton = (Button) findViewById(R.id.upload_button);
        mUploadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

                if (NetworkReceiver.running) {
                    ToastUtils.showShortToast(R.string.send_in_progress);
                } else if (ni == null || !ni.isConnected()) {
                    logger.logAction(this, "uploadButton", "noConnection");

                    ToastUtils.showShortToast(R.string.no_connection);
                } else {
                    int checkedItemCount = getCheckedCount();
                    logger.logAction(this, "uploadButton", Integer.toString(checkedItemCount));

                    if (checkedItemCount > 0) {
                        // items selected
                        uploadSelectedFiles();
                        InstanceUploaderList.this.getListView().clearChoices();
                        mUploadButton.setEnabled(false);
                    } else {
                        // no items selected
                        ToastUtils.showLongToast(R.string.noselect_error);
                    }
                }
            }
        });

        final Button toggleSelsButton = (Button) findViewById(R.id.toggle_button);
        toggleSelsButton.setLongClickable(true);
        toggleSelsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView lv = getListView();
                boolean allChecked = toggleChecked(lv);
                toggleButtonLabel(toggleSelsButton, lv);
                mUploadButton.setEnabled(allChecked);
            }
        });
        toggleSelsButton.setOnLongClickListener(this);

        setupAdapter(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setItemsCanFocus(false);
        mUploadButton.setEnabled(false);

        // set title
        setTitle(getString(R.string.send_data));

        instanceSyncTask = new InstanceSyncTask();
        instanceSyncTask.setDiskSyncListener(this);
        instanceSyncTask.execute();

        mSortingOptions = new String[]{
                getString(R.string.sort_by_name_asc), getString(R.string.sort_by_name_desc),
                getString(R.string.sort_by_date_asc), getString(R.string.sort_by_date_desc)
        };
    }

    @Override
    protected void onResume() {
        if (instanceSyncTask != null) {
            instanceSyncTask.setDiskSyncListener(this);
        }
        super.onResume();

        if (instanceSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
            syncComplete(instanceSyncTask.getStatusMessage());
        }
    }

    @Override
    protected void onPause() {
        if (instanceSyncTask != null) {
            instanceSyncTask.setDiskSyncListener(null);
        }
        super.onPause();
    }

    @Override
    public void syncComplete(String result) {
        TextView textView = (TextView) findViewById(R.id.status_text);
        textView.setText(result);
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

    private void uploadSelectedFiles() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String server = prefs.getString(PreferenceKeys.KEY_PROTOCOL, null);
        long[] instanceIds = getListView().getCheckedItemIds();
        if (server.equalsIgnoreCase(getString(R.string.protocol_google_sheets))) {
            // if it's Sheets, start the Sheets uploader
            // first make sure we have a google account selected

            if (PlayServicesUtil.isGooglePlayServicesAvailable(this)) {
                Intent i = new Intent(this, GoogleSheetsUploaderActivity.class);
                i.putExtra(FormEntryActivity.KEY_INSTANCES, instanceIds);
                startActivityForResult(i, INSTANCE_UPLOADER);
            } else {
                PlayServicesUtil.showGooglePlayServicesAvailabilityErrorDialog(this);
            }
        } else {
            // otherwise, do the normal aggregate/other thing.
            Intent i = new Intent(this, InstanceUploaderActivity.class);
            i.putExtra(FormEntryActivity.KEY_INSTANCES, instanceIds);
            startActivityForResult(i, INSTANCE_UPLOADER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logger.logAction(this, "onCreateOptionsMenu", "show");
        super.onCreateOptionsMenu(menu);

        menu
                .add(0, MENU_PREFERENCES, 0, R.string.general_preferences)
                .setIcon(R.drawable.ic_menu_preferences)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menu
                .add(0, MENU_SHOW_UNSENT, 1, R.string.change_view)
                .setIcon(R.drawable.ic_menu_manage)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                logger.logAction(this, "onMenuItemSelected", "MENU_PREFERENCES");
                createPreferencesMenu();
                return true;
            case MENU_SHOW_UNSENT:
                logger.logAction(this, "onMenuItemSelected", "MENU_SHOW_UNSENT");
                showSentAndUnsentChoices();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void createPreferencesMenu() {
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long rowId) {
        super.onListItemClick(listView, view, position, rowId);

        logger.logAction(this, "onListItemClick", Long.toString(rowId));

        if (getListView().isItemChecked(position)) {
            mSelectedInstances.add(getListView().getItemIdAtPosition(position));
        } else {
            mSelectedInstances.remove(getListView().getItemIdAtPosition(position));
        }

        mUploadButton.setEnabled(areCheckedItems());
        Button toggleSelectionsButton = (Button) findViewById(R.id.toggle_button);
        toggleButtonLabel(toggleSelectionsButton, getListView());
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        Log.d(t, "onRestoreInstanceState");
        super.onRestoreInstanceState(bundle);
        mUploadButton.setEnabled(areCheckedItems());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOW_ALL_MODE, mShowAllMode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        switch (requestCode) {
            // returns with a form path, start entry
            case INSTANCE_UPLOADER:
                if (intent.getBooleanExtra(FormEntryActivity.KEY_SUCCESS, false)) {
                    getListView().clearChoices();
                    if (mListAdapter.isEmpty()) {
                        finish();
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void setupAdapter(String sortOrder) {
        List<Long> checkedInstances = new ArrayList();
        for (long a : getListView().getCheckedItemIds()) {
            checkedInstances.add(a);
        }
        Cursor cursor;
        if (mShowAllMode) {
            cursor = mInstanceDao.getAllCompletedUndeletedInstancesCursor(sortOrder);
        } else {
            cursor = mInstanceDao.getFinalizedInstancesCursor(sortOrder);
        }
        String[] data = new String[]{InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT};
        int[] view = new int[]{R.id.text1, R.id.text2};

        mListAdapter = new SimpleCursorAdapter(this, R.layout.two_item_multiple_choice, cursor, data, view);
        setListAdapter(mListAdapter);
        checkPreviouslyCheckedItems();
    }

    @Override
    protected void filter(CharSequence charSequence) {
        if (mShowAllMode) {
            mListAdapter.changeCursor(mInstanceDao.getFilteredCompletedUndeletedInstancesCursor(charSequence));
        } else {
            mListAdapter.changeCursor(mInstanceDao.getFilteredFinalizedInstancesCursor(charSequence));
        }
        checkPreviouslyCheckedItems();
        mUploadButton.setEnabled(areCheckedItems());
    }

    private void showUnsent() {
        mShowAllMode = false;
        Cursor c = mInstanceDao.getFinalizedInstancesCursor();
        Cursor old = mListAdapter.getCursor();
        try {
            mListAdapter.changeCursor(c);
        } finally {
            if (old != null) {
                old.close();
                this.stopManagingCursor(old);
            }
        }
        getListView().invalidate();
    }

    private void showAll() {
        mShowAllMode = true;
        Cursor c = mInstanceDao.getAllCompletedUndeletedInstancesCursor();
        Cursor old = mListAdapter.getCursor();
        try {
            mListAdapter.changeCursor(c);
        } finally {
            if (old != null) {
                old.close();
                this.stopManagingCursor(old);
            }
        }
        getListView().invalidate();
    }

    @Override
    public boolean onLongClick(View v) {
        logger.logAction(this, "toggleButton.longClick", "");
        return showSentAndUnsentChoices();
    }

    private boolean showSentAndUnsentChoices() {
        /**
         * Create a dialog with options to save and exit, save, or quit without
         * saving
         */
        String[] items = {getString(R.string.show_unsent_forms),
                getString(R.string.show_sent_and_unsent_forms)};

        logger.logAction(this, "changeView", "show");

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(getString(R.string.change_view))
                .setNeutralButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                logger.logAction(this, "changeView", "cancel");
                                dialog.cancel();
                            }
                        })
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {

                            case 0: // show unsent
                                logger.logAction(this, "changeView", "showUnsent");
                                InstanceUploaderList.this.showUnsent();
                                break;

                            case 1: // show all
                                logger.logAction(this, "changeView", "showAll");
                                InstanceUploaderList.this.showAll();
                                break;

                            case 2:// do nothing
                                break;
                        }
                    }
                }).create();
        alertDialog.show();
        return true;
    }
}
