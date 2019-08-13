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
import androidx.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.InstanceUploaderAdapter;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.preferences.Transport;
import org.odk.collect.android.tasks.InstanceSyncTask;
import org.odk.collect.android.tasks.sms.SmsNotificationReceiver;
import org.odk.collect.android.tasks.sms.SmsService;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.PlayServicesUtil;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.List;

import javax.inject.Inject;

import androidx.work.State;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_SUBMISSION_TRANSPORT_TYPE;
import static org.odk.collect.android.tasks.sms.SmsSender.SMS_INSTANCE_ID;
import static org.odk.collect.android.utilities.PermissionUtils.finishAllActivities;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenuActivity}.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class InstanceUploaderListActivity extends InstanceListActivity implements
        OnLongClickListener, DiskSyncListener, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String SHOW_ALL_MODE = "showAllMode";
    private static final String INSTANCE_UPLOADER_LIST_SORTING_ORDER = "instanceUploaderListSortingOrder";

    private static final int INSTANCE_UPLOADER = 0;

    @BindView(R.id.upload_button)
    Button uploadButton;
    @BindView(R.id.sms_upload_button)
    Button smsUploadButton;
    @BindView(R.id.toggle_button)
    Button toggleSelsButton;

    private InstancesDao instancesDao;

    private InstanceSyncTask instanceSyncTask;

    private boolean showAllMode;

    // Default to true so the send button is disabled until the worker status is updated by the
    // observer
    private boolean autoSendOngoing = true;

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

    @Inject
    Tracker tracker;

    @Inject
    PermissionUtils permissionUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate");

        DaggerUtils.getComponent(this).inject(this);

        // set title
        setTitle(getString(R.string.send_data));
        setContentView(R.layout.instance_uploader_list);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            showAllMode = savedInstanceState.getBoolean(SHOW_ALL_MODE);
        }

        permissionUtils.requestStoragePermissions(this, new PermissionListener() {
            @Override
            public void granted() {
                init();
            }

            @Override
            public void denied() {
                // The activity has to finish because ODK Collect cannot function without these permissions.
                finishAllActivities(InstanceUploaderListActivity.this);
            }
        });
    }

    /**
     * Determines how an upload should be handled by checking the transport being used.
     * If the transport is set to SMS or Internet then either is used respectively else it's
     * "Both" so the button IDs drive the decision.
     *
     * @param button that triggers an upload
     */
    @OnClick({R.id.upload_button, R.id.sms_upload_button})
    public void onUploadButtonsClicked(Button button) {
        Transport transport = Transport.fromPreference(GeneralSharedPreferences.getInstance().get(KEY_SUBMISSION_TRANSPORT_TYPE));

        if (!transport.equals(Transport.Sms) && button.getId() == R.id.upload_button) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni == null || !ni.isConnected()) {
                ToastUtils.showShortToast(R.string.no_connection);
                return;
            }

            if (autoSendOngoing) {
                ToastUtils.showShortToast(R.string.send_in_progress);
                return;
            }
        }

        int checkedItemCount = getCheckedCount();

        if (checkedItemCount > 0) {
            // items selected
            uploadSelectedFiles(button.getId());
            setAllToCheckedState(listView, false);
            toggleButtonLabel(findViewById(R.id.toggle_button), listView);
            uploadButton.setEnabled(false);
            smsUploadButton.setEnabled(false);
        } else {
            // no items selected
            ToastUtils.showLongToast(R.string.noselect_error);
        }
    }

    /**
     * Changes the default upload button text if "Both" transport is
     * enabled and sets SMS upload button visibility
     */
    private void setupUploadButtons() {
        Transport transport = Transport.fromPreference(GeneralSharedPreferences.getInstance().get(KEY_SUBMISSION_TRANSPORT_TYPE));
        if (transport.equals(Transport.Both)) {
            uploadButton.setText(R.string.send_selected_data_internet);
            smsUploadButton.setVisibility(View.VISIBLE);
        } else {
            smsUploadButton.setVisibility(View.GONE);
            uploadButton.setText(R.string.send_selected_data);
        }
    }

    void init() {
        setupUploadButtons();
        instancesDao = new InstancesDao();

        toggleSelsButton.setLongClickable(true);
        toggleSelsButton.setOnClickListener(v -> {
            ListView lv = listView;
            boolean allChecked = toggleChecked(lv);
            toggleButtonLabel(toggleSelsButton, lv);
            uploadButton.setEnabled(allChecked);
            smsUploadButton.setEnabled(allChecked);
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
        listView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            uploadButton.setEnabled(areCheckedItems());
            smsUploadButton.setEnabled(areCheckedItems());
        });

        instanceSyncTask = new InstanceSyncTask();
        instanceSyncTask.setDiskSyncListener(this);
        instanceSyncTask.execute();

        sortingOptions = new int[] {
                R.string.sort_by_name_asc, R.string.sort_by_name_desc,
                R.string.sort_by_date_asc, R.string.sort_by_date_desc
        };

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        // Start observer that sets autoSendOngoing field based on AutoSendWorker status
        updateAutoSendStatus();
    }

    /**
     * Updates whether an auto-send job is ongoing.
     */
    private void updateAutoSendStatus() {
        LiveData<List<WorkStatus>> statuses = WorkManager.getInstance().getStatusesForUniqueWorkLiveData(AutoSendWorker.class.getName());

        statuses.observe(this, workStatuses -> {
            if (workStatuses != null) {
                for (WorkStatus status : workStatuses) {
                    if (status.getState().equals(State.RUNNING)) {
                        autoSendOngoing = true;
                        return;
                    }
                }
                autoSendOngoing = false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (instanceSyncTask != null) {
            instanceSyncTask.setDiskSyncListener(this);
            if (instanceSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
                syncComplete(instanceSyncTask.getStatusMessage());
            }

        }

        IntentFilter filter = new IntentFilter(SmsNotificationReceiver.SMS_NOTIFICATION_ACTION);
        // The default priority is 0. Positive values will be before
        // the default, lower values will be after it.
        filter.setPriority(1);

        registerReceiver(smsForegroundReceiver, filter);
        setupUploadButtons();
    }

    @Override
    protected void onPause() {
        if (instanceSyncTask != null) {
            instanceSyncTask.setDiskSyncListener(null);
        }
        try {
            unregisterReceiver(smsForegroundReceiver);
        } catch (IllegalArgumentException e) {
            Timber.w(e);
        }
        super.onPause();
    }

    @Override
    public void syncComplete(@NonNull String result) {
        Timber.i("Disk scan complete");
        hideProgressBarAndAllow();
        showSnackbar(result);
    }

    private void uploadSelectedFiles(int buttonId) {
        long[] instanceIds = listView.getCheckedItemIds();
        Transport transport = Transport.fromPreference(GeneralSharedPreferences.getInstance().get(KEY_SUBMISSION_TRANSPORT_TYPE));

        if (transport.equals(Transport.Sms) || buttonId == R.id.sms_upload_button) {
            // https://issuetracker.google.com/issues/66979952
            if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                permissionUtils.requestSendSMSAndReadPhoneStatePermissions(this, new PermissionListener() {
                    @Override
                    public void granted() {
                        smsService.submitForms(instanceIds);
                    }

                    @Override
                    public void denied() {
                    }
                });
            } else {
                permissionUtils.requestSendSMSPermission(this, new PermissionListener() {
                    @Override
                    public void granted() {
                        smsService.submitForms(instanceIds);
                    }

                    @Override
                    public void denied() {
                    }
                });
            }
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
                // Not required but without this permission a Device ID attached to a request will be empty.
                permissionUtils.requestReadPhoneStatePermission(this, false, new PermissionListener() {
                    @Override
                    public void granted() {
                        startActivityForResult(i, INSTANCE_UPLOADER);
                    }

                    @Override
                    public void denied() {
                        startActivityForResult(i, INSTANCE_UPLOADER);
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.instance_uploader_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_preferences:
                createPreferencesMenu();
                return true;
            case R.id.menu_change_view:
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
        if (listView.isItemChecked(position)) {
            selectedInstances.add(listView.getItemIdAtPosition(position));
        } else {
            selectedInstances.remove(listView.getItemIdAtPosition(position));
        }

        uploadButton.setEnabled(areCheckedItems());
        smsUploadButton.setEnabled(areCheckedItems());
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
            selectedInstances.clear();
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
        return showSentAndUnsentChoices();
    }

    /*
     * Create a dialog with options to save and exit, save, or quit without
     * saving
     */
    private boolean showSentAndUnsentChoices() {
        String[] items = {getString(R.string.show_unsent_forms),
                getString(R.string.show_sent_and_unsent_forms)};

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(getString(R.string.change_view))
                .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                    dialog.cancel();
                })
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0: // show unsent
                            showAllMode = false;
                            updateAdapter();
                            break;

                        case 1: // show all
                            showAllMode = true;
                            updateAdapter();
                            tracker.send(new HitBuilders.EventBuilder()
                                            .setCategory("FilterSendForms")
                                            .setAction("SentAndUnsent")
                                            .build());
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
