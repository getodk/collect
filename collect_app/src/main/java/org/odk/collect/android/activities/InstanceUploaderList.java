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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.InstanceUploaderAdapter;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.receivers.NetworkReceiver;
import org.odk.collect.android.tasks.InstanceSyncTask;
import org.odk.collect.android.tasks.sms.SmsNotificationReceiver;
import org.odk.collect.android.tasks.sms.SmsService;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.odk.collect.android.utilities.PlayServicesUtil;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SUBMISSION_TRANSPORT_TYPE;
import static org.odk.collect.android.tasks.sms.SmsSender.SMS_INSTANCE_ID;
import static org.odk.collect.android.utilities.PermissionUtils.finishAllActivities;
import static org.odk.collect.android.utilities.PermissionUtils.requestStoragePermissions;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenuActivity}.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class InstanceUploaderList extends InstanceListActivity implements
        OnLongClickListener, DiskSyncListener, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String SHOW_ALL_MODE = "showAllMode";
    private static final String INSTANCE_UPLOADER_LIST_SORTING_ORDER = "instanceUploaderListSortingOrder";

    private static final int INSTANCE_UPLOADER = 0;

    private Button uploadButton;

    private InstancesDao instancesDao;

    private InstanceSyncTask instanceSyncTask;

    private boolean showAllMode;

    private final BroadcastReceiver smsForegroundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Stops the notification from being sent to others that are listening for this broadcast.
            abortBroadcast();

            //deletes submission since the app is in the foreground and the NotificationReceiver won't be triggered.
            if (intent.hasExtra(SMS_INSTANCE_ID)) {
                deleteIfSubmissionCompleted(intent.getStringExtra(SMS_INSTANCE_ID));
            }
        }
    };

    @Inject
    SmsService smsService;
    @Inject
    SmsSubmissionManagerContract smsSubmissionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate");
        // set title
        setTitle(getString(R.string.send_data));
        setContentView(R.layout.instance_uploader_list);
        getComponent().inject(this);

        if (savedInstanceState != null) {
            showAllMode = savedInstanceState.getBoolean(SHOW_ALL_MODE);
        }

        requestStoragePermissions(this, new PermissionListener() {
            @Override
            public void granted() {
                init();
            }

            @Override
            public void denied() {
                // The activity has to finish because ODK Collect cannot function without these permissions.
                finishAllActivities(InstanceUploaderList.this);
            }
        });
    }

    private void init() {
        instancesDao = new InstancesDao();
        uploadButton = findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String transport = (String) GeneralSharedPreferences.getInstance().get(KEY_SUBMISSION_TRANSPORT_TYPE);
                if (!transport.equalsIgnoreCase(getString(R.string.transport_type_value_sms))) {

                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                            Context.CONNECTIVITY_SERVICE);
                    NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

                    if (NetworkReceiver.running) {
                        ToastUtils.showShortToast(R.string.send_in_progress);
                        return;
                    } else if (ni == null || !ni.isConnected()) {
                        logger.logAction(this, "uploadButton", "noConnection");
                        ToastUtils.showShortToast(R.string.no_connection);
                        return;

                    }
                }

                int checkedItemCount = getCheckedCount();
                logger.logAction(this, "uploadButton", Integer.toString(checkedItemCount));

                if (checkedItemCount > 0) {
                    // items selected
                    uploadSelectedFiles();
                    setAllToCheckedState(listView, false);
                    toggleButtonLabel(findViewById(R.id.toggle_button), listView);
                    uploadButton.setEnabled(false);
                } else {
                    // no items selected
                    ToastUtils.showLongToast(R.string.noselect_error);
                }
            }
        });

        final Button toggleSelsButton = findViewById(R.id.toggle_button);
        toggleSelsButton.setLongClickable(true);
        toggleSelsButton.setOnClickListener(v -> {
            ListView lv = listView;
            boolean allChecked = toggleChecked(lv);
            toggleButtonLabel(toggleSelsButton, lv);
            uploadButton.setEnabled(allChecked);
            if (allChecked) {
                for (int i = 0; i < lv.getCount(); i++) {
                    selectedInstances.add(lv.getItemIdAtPosition(i));
                }
            } else {
                selectedInstances.clear();
            }
        });
        toggleSelsButton.setOnLongClickListener(this);

        setupAdapter();

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);
        listView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> uploadButton.setEnabled(areCheckedItems()));


        instanceSyncTask = new InstanceSyncTask();
        instanceSyncTask.setDiskSyncListener(this);
        instanceSyncTask.execute();

        sortingOptions = new String[]{
                getString(R.string.sort_by_name_asc), getString(R.string.sort_by_name_desc),
                getString(R.string.sort_by_date_asc), getString(R.string.sort_by_date_desc)
        };

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        if (instanceSyncTask != null) {
            instanceSyncTask.setDiskSyncListener(this);
            if (instanceSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
                syncComplete(instanceSyncTask.getStatusMessage());
            }

        }
        super.onResume();

        IntentFilter filter = new IntentFilter(SmsNotificationReceiver.SMS_NOTIFICATION_ACTION);
        // The default priority is 0. Positive values will be before
        // the default, lower values will be after it.
        filter.setPriority(1);

        registerReceiver(smsForegroundReceiver, filter);
    }

    @Override
    protected void onPause() {
        if (instanceSyncTask != null) {
            instanceSyncTask.setDiskSyncListener(null);
        }
        super.onPause();

        unregisterReceiver(smsForegroundReceiver);
    }

    @Override
    public void syncComplete(@NonNull String result) {
        Timber.i("Disk scan complete");
        hideProgressBarAndAllow();
        showSnackbar(result);
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
        long[] instanceIds = listView.getCheckedItemIds();
        String transport = (String) GeneralSharedPreferences.getInstance().get(KEY_SUBMISSION_TRANSPORT_TYPE);

        if (transport.equalsIgnoreCase(getString(R.string.transport_type_value_sms))) {
            smsService.submitForms(instanceIds);
        } else {

            String server = (String) GeneralSharedPreferences.getInstance().get(KEY_PROTOCOL);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logger.logAction(this, "onCreateOptionsMenu", "show");
        getMenuInflater().inflate(R.menu.instance_uploader_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_preferences:
                logger.logAction(this, "onMenuItemSelected", "MENU_PREFERENCES");
                createPreferencesMenu();
                return true;
            case R.id.menu_change_view:
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
        Button toggleSelectionsButton = findViewById(R.id.toggle_button);
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
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void setupAdapter() {
        List<Long> checkedInstances = new ArrayList();
        for (long a : listView.getCheckedItemIds()) {
            checkedInstances.add(a);
        }

        listAdapter = new InstanceUploaderAdapter(this, null);
        listView.setAdapter(listAdapter);
        checkPreviouslyCheckedItems();
    }

    @Override
    protected String getSortingOrderKey() {
        return INSTANCE_UPLOADER_LIST_SORTING_ORDER;
    }

    @Override
    protected void updateAdapter() {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showProgressBar();
        if (showAllMode) {
            return instancesDao.getCompletedUndeletedInstancesCursorLoader(getFilterText(), getSortingOrder());
        } else {
            return instancesDao.getFinalizedInstancesCursorLoader(getFilterText(), getSortingOrder());
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        hideProgressBarIfAllowed();
        listAdapter.changeCursor(cursor);
        checkPreviouslyCheckedItems();
        toggleButtonLabel(findViewById(R.id.toggle_button), listView);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        listAdapter.swapCursor(null);
    }

    @Override
    public boolean onLongClick(View v) {
        logger.logAction(this, "toggleButton.longClick", "");
        return showSentAndUnsentChoices();
    }

    /*
     * Create a dialog with options to save and exit, save, or quit without
     * saving
     */
    private boolean showSentAndUnsentChoices() {
        String[] items = {getString(R.string.show_unsent_forms),
                getString(R.string.show_sent_and_unsent_forms)};

        logger.logAction(this, "changeView", "show");

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(getString(R.string.change_view))
                .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                    logger.logAction(this, "changeView", "cancel");
                    dialog.cancel();
                })
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0: // show unsent
                            logger.logAction(this, "changeView", "showUnsent");
                            showAllMode = false;
                            updateAdapter();
                            break;

                        case 1: // show all
                            logger.logAction(this, "changeView", "showAll");
                            showAllMode = true;
                            updateAdapter();
                            break;

                        case 2:// do nothing
                            break;
                    }
                }).create();
        alertDialog.show();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (listAdapter != null) {
            ((InstanceUploaderAdapter) listAdapter).onDestroy();
        }
    }

    private void deleteIfSubmissionCompleted(String instanceId) {
        SmsSubmission model = smsSubmissionManager.getSubmissionModel(instanceId);
        if (model.isSubmissionComplete()) {
            smsSubmissionManager.forgetSubmission(model.getInstanceId());
        }
    }
}
