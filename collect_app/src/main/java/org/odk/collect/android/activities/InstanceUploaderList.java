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
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.receivers.NetworkReceiver;
import org.odk.collect.android.tasks.InstanceSyncTask;
import org.odk.collect.android.utilities.PlayServicesUtil;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenuActivity}.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class InstanceUploaderList extends InstanceListActivity
        implements OnLongClickListener, DiskSyncListener, AdapterView.OnItemClickListener {
    private static final String SHOW_ALL_MODE = "showAllMode";
    private static final String INSTANCE_UPLOADER_LIST_SORTING_ORDER = "instanceUploaderListSortingOrder";

    private static final int MENU_PREFERENCES = Menu.FIRST;
    private static final int MENU_SHOW_UNSENT = MENU_PREFERENCES + 1;

    private static final int INSTANCE_UPLOADER = 0;

    private Button uploadButton;

    private InstancesDao instancesDao;

    private InstanceSyncTask instanceSyncTask;

    private boolean showAllMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Timber.i("onCreate");
        setContentView(R.layout.instance_uploader_list);
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            showAllMode = savedInstanceState.getBoolean(SHOW_ALL_MODE);
        }

        instancesDao = new InstancesDao();

        uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new OnClickListener() {

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
                        InstanceUploaderList.this.listView.clearChoices();
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
                ListView lv = listView;
                boolean allChecked = toggleChecked(lv);
                toggleButtonLabel(toggleSelsButton, lv);
                uploadButton.setEnabled(allChecked);
            }
        });
        toggleSelsButton.setOnLongClickListener(this);

        setupAdapter();

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);
        listView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                uploadButton.setEnabled(areCheckedItems());
            }
        });

        // set title
        setTitle(getString(R.string.send_data));

        instanceSyncTask = new InstanceSyncTask();
        instanceSyncTask.setDiskSyncListener(this);
        instanceSyncTask.execute();

        sortingOptions = new String[]{
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
        String server = (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_PROTOCOL);
        long[] instanceIds = listView.getCheckedItemIds();
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
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menu
                .add(0, MENU_SHOW_UNSENT, 1, R.string.change_view)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        return super.onOptionsItemSelected(item);
    }

    private void createPreferencesMenu() {
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
        logger.logAction(this, "onListItemClick", Long.toString(rowId));

        if (listView.isItemChecked(position)) {
            selectedInstances.add(listView.getItemIdAtPosition(position));
        } else {
            selectedInstances.remove(listView.getItemIdAtPosition(position));
        }

        uploadButton.setEnabled(areCheckedItems());
        Button toggleSelectionsButton = (Button) findViewById(R.id.toggle_button);
        toggleButtonLabel(toggleSelectionsButton, listView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOW_ALL_MODE, showAllMode);
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
                    listView.clearChoices();
                    if (listAdapter.isEmpty()) {
                        finish();
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void setupAdapter() {
        List<Long> checkedInstances = new ArrayList();
        for (long a : listView.getCheckedItemIds()) {
            checkedInstances.add(a);
        }
        String[] data = new String[]{InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT};
        int[] view = new int[]{R.id.text1, R.id.text2};

        listAdapter = new SimpleCursorAdapter(this, R.layout.two_item_multiple_choice, getCursor(), data, view);
        listView.setAdapter(listAdapter);
        checkPreviouslyCheckedItems();
    }

    @Override
    protected String getSortingOrderKey() {
        return INSTANCE_UPLOADER_LIST_SORTING_ORDER;
    }

    @Override
    protected void updateAdapter() {
        listAdapter.changeCursor(getCursor());
        checkPreviouslyCheckedItems();
    }

    private Cursor getCursor() {
        Cursor cursor;
        if (showAllMode) {
            cursor = instancesDao.getCompletedUndeletedInstancesCursor(getFilterText(), getSortingOrder());
        } else {
            cursor = instancesDao.getFinalizedInstancesCursor(getFilterText(), getSortingOrder());
        }

        return cursor;
    }

    private void showUnsent() {
        showAllMode = false;
        Cursor old = listAdapter.getCursor();
        try {
            listAdapter.changeCursor(getCursor());
        } finally {
            if (old != null) {
                old.close();
                this.stopManagingCursor(old);
            }
        }
        listView.invalidate();
    }

    private void showAll() {
        showAllMode = true;
        Cursor old = listAdapter.getCursor();
        try {
            listAdapter.changeCursor(getCursor());
        } finally {
            if (old != null) {
                old.close();
                this.stopManagingCursor(old);
            }
        }
        listView.invalidate();
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
