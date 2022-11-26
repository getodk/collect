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

package org.odk.collect.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.SurveyDataViewModel;
import org.odk.collect.android.activities.viewmodels.SurveyDataViewModelFactory;
import org.odk.collect.android.adapters.ViewPagerAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.configure.legacy.LegacySettingsFileImporter;
import org.odk.collect.android.fragments.SmapFormListFragment;
import org.odk.collect.android.fragments.SmapTaskListFragment;
import org.odk.collect.android.fragments.SmapTaskMapFragment;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.listeners.NFCListener;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.listeners.TaskDownloaderListener;
import org.odk.collect.android.loaders.SurveyData;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.services.LocationService;
import org.odk.collect.android.smap.formmanagement.ServerFormDetailsSmap;
import org.odk.collect.android.smap.listeners.DownloadFormsTaskListenerSmap;
import org.odk.collect.android.smap.utilities.LocationRegister;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.storage.migration.StorageMigrationDialog;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.storage.migration.StorageMigrationResult;
import org.odk.collect.android.taskModel.FormLaunchDetail;
import org.odk.collect.android.taskModel.FormRestartDetails;
import org.odk.collect.android.taskModel.NfcTrigger;
import org.odk.collect.android.tasks.DownloadTasksTask;
import org.odk.collect.android.tasks.NdefReaderTask;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.ManageForm;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.SnackbarUtils;
import org.odk.collect.android.utilities.Utilities;
import org.odk.collect.material.MaterialBanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static org.odk.collect.android.utilities.DialogUtils.getDialog;
import static org.odk.collect.android.utilities.DialogUtils.showIfNotShowing;

public class SmapMain extends CollectAbstractActivity implements TaskDownloaderListener,
        NFCListener,
        InstanceUploaderListener,
        DownloadFormsTaskListenerSmap {

    private static final int PROGRESS_DIALOG = 1;
    private static final int ALERT_DIALOG = 2;
    private static final int PASSWORD_DIALOG = 3;
    private static final int COMPLETE_FORM = 4;

    private ProgressDialog mProgressDialog;
    private String mAlertMsg;
    private boolean mPaused = false;

    public static final String EXTRA_REFRESH = "refresh";
    public static final String LOGIN_STATUS = "login_status";

    private final SmapFormListFragment formManagerList = SmapFormListFragment.newInstance();
    private final SmapTaskListFragment taskManagerList = SmapTaskListFragment.newInstance();
    private final SmapTaskMapFragment taskManagerMap = SmapTaskMapFragment.newInstance();

    private NfcAdapter mNfcAdapter;        // NFC
    public PendingIntent mNfcPendingIntent;
    public IntentFilter[] mNfcFilters;
    public NdefReaderTask mReadNFC;
    public ArrayList<NfcTrigger> nfcTriggersList;   // nfcTriggers (geofence should have separate list)

    private String mProgressMsg;
    public DownloadTasksTask mDownloadTasks;
    private Activity currentActivity;

    SurveyDataViewModel model;
    private MainTaskListener listener = null;
    private RefreshListener refreshListener = null;

    boolean listenerRegistered = false;
    private static List<TaskEntry> mTasks = null;

    private Intent mLocationServiceIntent = null;
    private LocationService mLocationService = null;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;

    @Inject
    PermissionsProvider permissionsProvider;

    /*
     * Start scoped storage
     */

    @BindView(R.id.storageMigrationBannerSmap)
    MaterialBanner storageMigrationBanner;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    @Inject
    StorageStateProvider storageStateProvider;

    @Inject
    SettingsImporter settingsImporter;

    @Inject
    StoragePathProvider storagePathProvider;

    // End scoped storage


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(getString(R.string.app_name));
        toolbar.setNavigationIcon(R.mipmap.ic_nav);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smap_main_layout);
        ButterKnife.bind(this);

        DaggerUtils.getComponent(this).inject(this);

        storageMigrationRepository.getResult().observe(this, this::onStorageMigrationFinish);

        String[] tabNames = {getString(R.string.smap_forms), getString(R.string.smap_tasks), getString(R.string.smap_map)};
        // Get the ViewPager and set its PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(formManagerList);
        fragments.add(taskManagerList);
        fragments.add(taskManagerMap);

        viewPager.setAdapter(new ViewPagerAdapter(
                getSupportFragmentManager(), tabNames, fragments));

        // Give the SlidingTabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabLayout.setBackgroundColor(getResources().getColor(R.color.tabBackground));

        tabLayout.setTabTextColors(Color.LTGRAY, Color.WHITE);
        tabLayout.setupWithViewPager(viewPager);

        stateChanged();

        // Show login status if it was set
        String login_status = getIntent().getStringExtra(LOGIN_STATUS);
        if(login_status != null) {
            if(login_status.equals("success")) {
                SnackbarUtils.showShortSnackbar(findViewById(R.id.pager), Collect.getInstance().getString(R.string.smap_login_success));
                Utilities.updateServerRegistration(false);     // Update the server registration
            } else if(login_status.equals("failed")) {
                SnackbarUtils.showShortSnackbar(findViewById(R.id.pager), Collect.getInstance().getString(R.string.smap_login_failed));
            }
        }

        // Restore the preference to record a user trail in case the user had previously selected "exit"
        GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_SMAP_USER_LOCATION,
                GeneralSharedPreferences.getInstance().getBoolean(GeneralKeys.KEY_SMAP_USER_SAVE_LOCATION, false));

        // Initiate a refresh if requested in start parameters
        String refresh = getIntent().getStringExtra(EXTRA_REFRESH);
        if(refresh != null && refresh.equals("yes")) {
            processGetTask(true);   // Set manual true so that refresh after logon works (logon = manual refresh request)
        }

        // Start the location service
        currentActivity = this;
        LocationRegister lr = new LocationRegister();
        if(lr.locationEnabled()) {
            permissionsProvider.requestLocationPermissions(this, new PermissionListener() {
                @Override
                public void granted() {

                    permissionsProvider.requestBackgroundLocationPermissions(currentActivity, new PermissionListener() {
                        @Override
                        public void granted() {
                            startLocationService();
                        }

                        @Override
                        public void denied() {
                            startLocationService();     // Start the service anyway it will only work when the app is in the foreground
                        }
                    });

                }

                @Override
                public void denied() {
                }
            });
        }

        LegacySettingsFileImporter legacySettingsFileImporter = new LegacySettingsFileImporter(storagePathProvider, null, settingsImporter);
        if (legacySettingsFileImporter.importFromFile()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.successfully_imported_settings)
                    .setMessage(R.string.settings_successfully_loaded_file_notification)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                        recreate();
                    })
                    .setCancelable(false)
                    .create().show();
        }
    }

    public SurveyDataViewModel getViewModel() {
        return model;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_nav);
        stateChanged();
    }

    /*
     * Start a foreground service
     */
    public void startLocationService() {

        mLocationService = new LocationService();
        mLocationServiceIntent = new Intent(Collect.getInstance().getApplicationContext(), mLocationService.getClass());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(mLocationServiceIntent);
        } else {
            startService(mLocationServiceIntent);
        }
    }

    /*
     * Do all the actions required on create or rotate
     */
    private void stateChanged() {

        initToolbar();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SurveyDataViewModelFactory viewModelFactory = new SurveyDataViewModelFactory(sharedPreferences);

        model = new ViewModelProvider(this, viewModelFactory).get(SurveyDataViewModel.class);
        model.getSurveyData().observe(this, surveyData -> {
            // update U
            Timber.i("-------------------------------------- Smap Main Activity got Data ");
            updateData(surveyData);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPaused = false;

        if (!listenerRegistered) {
            listener = new MainTaskListener(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction("startTask");
            registerReceiver(listener, filter);

            refreshListener = new RefreshListener(this);   // Listen for updates to the form list

            listenerRegistered = true;
        }

        // NFC
        boolean nfcAuthorised = false;
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        if (sharedPreferences.getBoolean(GeneralKeys.KEY_SMAP_LOCATION_TRIGGER, true)) {
            if(mNfcAdapter == null) {
                mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            }

            if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {

                // Pending intent
                Intent nfcIntent = new Intent(getApplicationContext(), getClass());
                nfcIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                if(mNfcPendingIntent == null) {
                    mNfcPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, nfcIntent,
                            PendingIntent.FLAG_MUTABLE);    // Must be mutable
                }

                if(mNfcFilters == null) {
                    // Filter
                    IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
                    mNfcFilters = new IntentFilter[]{
                            filter
                    };
                }

                mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNfcFilters, null);

            }
        }

        setUpStorageMigrationBanner();
        tryToPerformAutomaticMigration();
    }

    @Override
    protected void onDestroy() {
        if(mLocationService != null) {
            stopService(mLocationServiceIntent);
        }
        storageMigrationRepository.clearResult();
        super.onDestroy();

    }

    public void processAdminMenu() {
        showDialog(PASSWORD_DIALOG);
    }

    // Get tasks and forms from the server
    public void processGetTask(boolean manual) {

      if(!storageMigrationRepository.isMigrationBeingPerformed() && (manual || Utilities.isFormAutoSendOptionEnabled())) {
            mDownloadTasks = new DownloadTasksTask();
            if(manual) {
                mProgressMsg = getString(R.string.smap_synchronising);
                if (!this.isFinishing()) {
                    showDialog(PROGRESS_DIALOG);
                }
                mDownloadTasks.setDownloaderListener(this, this);
            }
            mDownloadTasks.execute();
        }
    }

    public void processHistory() {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            Intent i = new Intent(getApplicationContext(), HistoryActivity.class);
            i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                    ApplicationConstants.FormModes.VIEW_SENT);
            startActivity(i);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (mDownloadTasks != null) {
                                    mDownloadTasks.setDownloaderListener(null, SmapMain.this);
                                    mDownloadTasks.cancel(true);
                                }
                                // Refresh the task list
                                Intent intent = new Intent("org.smap.smapTask.refresh");
                                LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);
                                Timber.i("######## send org.smap.smapTask.refresh from smapMain");  // smap
                            }
                        };
                mProgressDialog.setTitle(getString(R.string.downloading_data));
                mProgressDialog.setMessage(mProgressMsg);
                mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
            case ALERT_DIALOG:
                AlertDialog mAlertDialog = new AlertDialog.Builder(this).create();
                mAlertDialog.setMessage(mAlertMsg);
                mAlertDialog.setTitle(getString(R.string.smap_get_tasks));
                DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                };
                mAlertDialog.setCancelable(false);
                mAlertDialog.setButton(getString(R.string.ok), quitListener);
                mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
                return mAlertDialog;
            case PASSWORD_DIALOG:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final AlertDialog passwordDialog = builder.create();
                final SharedPreferences adminPreferences = this.getSharedPreferences(
                        AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

                passwordDialog.setTitle(getString(R.string.enter_admin_password));
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setTransformationMethod(PasswordTransformationMethod
                        .getInstance());
                passwordDialog.setView(input, 20, 10, 20, 10);

                passwordDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                        getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = input.getText().toString();
                                String pw = adminPreferences.getString(
                                        AdminKeys.KEY_ADMIN_PW, "");
                                if (pw.compareTo(value) == 0) {
                                    Intent i = new Intent(getApplicationContext(),
                                            AdminPreferencesActivity.class);
                                    startActivity(i);
                                    input.setText("");
                                    passwordDialog.dismiss();
                                } else {
                                    Toast.makeText(
                                            SmapMain.this,
                                            getString(R.string.admin_password_incorrect),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                passwordDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                input.setText("");
                                return;
                            }
                        });

                passwordDialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return passwordDialog;
        }
        return null;
    }

    /*
     * Forms Downloading Overrides
     */
    @Override
    public void formsDownloadingComplete(Map<ServerFormDetailsSmap, String> result) {
        // TODO Auto-generated method stub
        // Ignore formsDownloading is called synchronously from taskDownloader
    }

    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
        // TODO Auto-generated method stub
        mProgressMsg = getString(R.string.smap_checking_file, currentFile, String.valueOf(progress), String.valueOf(total));
        if(mProgressDialog != null) {
            mProgressDialog.setMessage(mProgressMsg);
        }
    }

    @Override
    public void formsDownloadingCancelled() {
       // ignore
    }

    /*
     * Task Download overrides
     */
    @Override
    // Download tasks progress update
    public void progressUpdate(String progress) {
        if(mProgressMsg != null && mProgressDialog != null) {
            mProgressMsg = progress;
            mProgressDialog.setMessage(mProgressMsg);
        }
    }

    @Override
    public void taskDownloadingComplete(HashMap<String, String> result) {

        Timber.i("Complete - Send intent");

        try {
            dismissDialog(PROGRESS_DIALOG);
            removeDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }
        try {
            dismissDialog(ALERT_DIALOG);
            removeDialog(ALERT_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }

        if (result != null) {
            StringBuilder message = new StringBuilder();
            Set<String> keys = result.keySet();
            Iterator<String> it = keys.iterator();

            if(it != null) {
                while (it.hasNext()) {
                    String key = it.next();
                    if (key.equals("err_not_enabled")) {
                        message.append(this.getString(R.string.smap_tasks_not_enabled));
                    } else if (key.equals("err_no_tasks")) {
                        // No tasks is fine, in fact its the most common state
                        //message.append(this.getString(R.string.smap_no_tasks));
                    } else {
                        message.append(key + " - " + result.get(key) + "\n\n");
                    }
                }
            }

            mAlertMsg = message.toString().trim();
            if (mAlertMsg.length() > 0) {
                try {
                    showDialog(ALERT_DIALOG);
                } catch (Exception e) {
                    // Tried to show a dialog but the activity may have been closed don't care
                    // However presumably this dialog showing should be replaced by use of progress bar
                }
            }

        }
    }

    /*
     * Uploading overrides
     */
    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void progressUpdate(int progress, int total) {
        mAlertMsg = getString(R.string.sending_items, String.valueOf(progress), String.valueOf(total));
        mProgressDialog.setMessage(mAlertMsg);
    }

    @Override
    public void authRequest(Uri url, HashMap<String, String> doneSoFar) {
        // TODO Auto-generated method stub

    }

    /*
     * NFC Reading Overrides
     */


    /**
     * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopNFCDispatch(final Activity activity, NfcAdapter adapter) {

        if (adapter != null) {
            adapter.disableForegroundDispatch(activity);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNFCIntent(intent);
    }

    /*
     * NFC detected
     */
    private void handleNFCIntent(Intent intent) {

        if (nfcTriggersList != null && nfcTriggersList.size() > 0) {
            Timber.i("tag discovered");
            String action = intent.getAction();
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            mReadNFC = new NdefReaderTask();
            mReadNFC.setDownloaderListener(this);
            mReadNFC.execute(tag);
        } else {
            Toast.makeText(
                    this,
                    R.string.smap_no_tasks_nfc,
                    Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public void readComplete(String result) {

        boolean foundTask = false;

        if (nfcTriggersList != null) {
            for (NfcTrigger trigger : nfcTriggersList) {
                if (trigger.uid.equals(result)) {
                    foundTask = true;

                    Intent i = new Intent();
                    i.setAction("startTask");
                    i.putExtra("position", trigger.position);
                    sendBroadcast(i);

                    Toast.makeText(
                            SmapMain.this,
                            getString(R.string.smap_starting_task_from_nfc, result),
                            Toast.LENGTH_LONG).show();

                    break;
                }
            }
        }
        if (!foundTask) {
            Toast.makeText(
                    SmapMain.this,
                    getString(R.string.smap_no_matching_tasks_nfc, result),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == COMPLETE_FORM && intent != null) {

            String instanceId = intent.getStringExtra("instanceid");
            String formStatus = intent.getStringExtra("status");
            String formURI = intent.getStringExtra("uri");

            formCompleted(instanceId, formStatus, formURI);
        }
    }

    /*
     * The user has selected an option to edit / complete a task
     * If the activity has been paused then a task has already been launched so ignore
     * Unless this request comes not from a user click but from code in which case force the launch
     */
    public void completeTask(TaskEntry entry, boolean force) {

        if(!storageMigrationRepository.isMigrationBeingPerformed() && (!mPaused || force)) {
            String surveyNotes = null;
            String formPath = new StoragePathProvider().getDirPath(StorageSubdirectory.FORMS) + entry.taskForm;
            String instancePath = entry.instancePath;
            long taskId = entry.id;
            String status = entry.taskStatus;

            // set the adhoc location
            boolean canUpdate = Utilities.canComplete(status, entry.taskType);
            boolean isSubmitted = Utilities.isSubmitted(status);
            boolean isSelfAssigned = Utilities.isSelfAssigned(status);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean reviewFinal = sharedPreferences.getBoolean(GeneralKeys.KEY_SMAP_REVIEW_FINAL, true);

            if (isSubmitted) {
                Toast.makeText(
                        SmapMain.this,
                        getString(R.string.smap_been_submitted),
                        Toast.LENGTH_LONG).show();
            } else if (!canUpdate && reviewFinal) {
                // Show a message if this task is read only
                if(isSelfAssigned) {
                    Toast.makeText(
                            SmapMain.this,
                            getString(R.string.smap_self_select),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(
                            SmapMain.this,
                            getString(R.string.read_only),
                            Toast.LENGTH_LONG).show();
                }
            } else if (!canUpdate && !reviewFinal) {
                // Show a message if this task is read only and cannot be reviewed
                Toast.makeText(
                        SmapMain.this,
                        getString(R.string.no_review),
                        Toast.LENGTH_LONG).show();
            }

            // Open the task if it is editable or reviewable
            if ((canUpdate || reviewFinal) && !isSubmitted && !isSelfAssigned) {
                // Get the provider URI of the instance
                String where = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + "=?";
                String[] whereArgs = {
                        instancePath
                };

                Timber.i("Complete Task: " + entry.id + " : " + entry.name + " : "
                        + entry.taskStatus + " : " + instancePath);

                Cursor cInstanceProvider = Collect.getInstance().getContentResolver().query(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                        null, where, whereArgs, null);

                if (entry.repeat) {
                    entry.instancePath = duplicateInstance(formPath, entry.instancePath, entry);
                }

                if (cInstanceProvider.moveToFirst()) {
                    long idx = cInstanceProvider.getLong(cInstanceProvider.getColumnIndexOrThrow(InstanceProviderAPI.InstanceColumns._ID));
                    if (idx > 0) {
                        Uri instanceUri = ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI, idx);
                        surveyNotes = cInstanceProvider.getString(
                                cInstanceProvider.getColumnIndexOrThrow(InstanceProviderAPI.InstanceColumns.T_SURVEY_NOTES));
                        // Start activity to complete form

                        // Use an explicit intent
                        Intent i = new Intent(this, org.odk.collect.android.activities.FormEntryActivity.class);
                        i.setData(instanceUri);

                        i.putExtra(FormEntryActivity.KEY_TASK, taskId);
                        i.putExtra(FormEntryActivity.KEY_SURVEY_NOTES, surveyNotes);
                        i.putExtra(FormEntryActivity.KEY_CAN_UPDATE, canUpdate);
                        i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
                        if (entry.formIndex != null) {
                            FormRestartDetails frd = new FormRestartDetails();
                            frd.initiatingQuestion = entry.formIndex;
                            frd.launchedFormStatus = entry.formStatus;
                            frd.launchedFormInstanceId = entry.instanceId;
                            frd.launchedFormURI = entry.formURI;
                            Collect.getInstance().setFormRestartDetails(frd);
                        }
                        if (instancePath != null) {    // TODO Don't think this is needed
                            i.putExtra(FormEntryActivity.KEY_INSTANCEPATH, instancePath);
                        }
                        startActivityForResult(i, COMPLETE_FORM);

                        // If More than one instance is found pointing towards a single file path then report the error and delete the extrat
                        int instanceCount = cInstanceProvider.getCount();
                        if (instanceCount > 1) {
                            Timber.e(new Exception("Unique instance not found: deleting extra, count is:" +
                                    cInstanceProvider.getCount()));
                        }
                    }
                } else {
                    Timber.e(new Exception("Task not found for instance path:" + instancePath));
                }

                cInstanceProvider.close();
            }
        } else {
            Timber.i("##################: Task launch blocked");
        }

    }

    /*
     * The user has selected an option to edit / complete a form
     * The force parameter can be used to force launching of the new form even when the smap activity is paused
     */
    public void completeForm(TaskEntry entry, boolean force, String initialData) {
        if(!storageMigrationRepository.isMigrationBeingPerformed() && (!mPaused || force)) {
            Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, entry.id);

            // Use an explicit intent
            Intent i = new Intent(this, org.odk.collect.android.activities.FormEntryActivity.class);
            i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
            i.putExtra(FormEntryActivity.KEY_READ_ONLY, entry.readOnly);
            i.setData(formUri);
            if(initialData != null) {
                i.putExtra(FormEntryActivity.KEY_INITIAL_DATA, initialData);
            }
            startActivityForResult(i, COMPLETE_FORM);
        } else {
            Timber.i("################# form launch blocked");
        }
    }

    /*
     * respond to completion of a form
     */
    public void formCompleted(String instanceId, String formStatus, String formURI) {
        Timber.i("Form completed");
        FormLaunchDetail fld = Collect.getInstance().popFromFormStack();
        TaskEntry te = new TaskEntry();
        if(fld != null) {
            if(fld.id > 0) {
                // Start a form
                te.id = fld.id;

                SnackbarUtils.showLongSnackbar(findViewById(R.id.llParent),
                        Collect.getInstance().getString(R.string.smap_starting_form, fld.formName));

                completeForm(te, true, fld.initialData);
            } else if(fld.instancePath != null) {
                // Start a task or saved instance
                te.id = 0;
                te.instancePath = fld.instancePath;
                te.taskStatus = Utilities.STATUS_T_ACCEPTED;
                te.repeat = false;
                te.formIndex = fld.formIndex;
                te.instanceId = instanceId;
                te.formStatus = formStatus;
                te.formURI = formURI;

                SnackbarUtils.showLongSnackbar(findViewById(R.id.pager),
                        Collect.getInstance().getString(R.string.smap_restarting_form, fld.formName));

                completeTask(te, true);
            }
        } else {
            if(formStatus != null && formStatus.equals("complete")) {
                processGetTask(false);
            }
        }
    }

    /*
     * Duplicate the instance
     * Call this if the instance repeats
     */
    public String duplicateInstance(String formPath, String originalPath, TaskEntry entry) {
        String newPath = null;

        // 1. Get a new instance path
        ManageForm mf = new ManageForm();
        newPath = mf.getInstancePath(formPath, entry.assId, null);

        // 2. Duplicate the instance entry and get the new path
        Utilities.duplicateTask(originalPath, newPath, entry);

        // 3. Copy the instance files
        Utilities.copyInstanceFiles(originalPath, newPath, formPath);
        return newPath;
    }

    /*
     * Get the tasks shown on the map
     */
    public List<TaskEntry> getTasks() {
        return mTasks;
    }

    /*
     * Manage location triggers
     */
    public void setLocationTriggers(List<TaskEntry> data) {

        mTasks = data;
        nfcTriggersList = new ArrayList<NfcTrigger>();

        /*
         * Set NFC triggers
         */

        int position = 0;
        for (TaskEntry t : data) {
            if (t.type.equals("task") && t.locationTrigger != null && t.locationTrigger.trim().length() > 0
                    && t.taskStatus.equals(Utilities.STATUS_T_ACCEPTED)) {
                nfcTriggersList.add(new NfcTrigger(t.id, t.locationTrigger, position));
            }
            position++;
        }

    }

    /*
     * Update fragments that use data sourced from the loader that called this method
     */
    public void updateData(SurveyData data) {
        formManagerList.setData(data); // loader
        taskManagerList.setData(data);
        taskManagerMap.setData(data);
        if(data != null) {
            setLocationTriggers(data.tasks);      // NFC and geofence triggers
        }
    }

    protected class MainTaskListener extends BroadcastReceiver {

        private SmapMain mActivity = null;

        public MainTaskListener(SmapMain activity) {
            mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            Timber.i("Intent received: %s", intent.getAction());

            if (intent.getAction().equals("startTask")) {

                int position = intent.getIntExtra("position", -1);
                if (position >= 0) {
                    TaskEntry entry = (TaskEntry) mTasks.get(position);

                    mActivity.completeTask(entry, true);
                }
            }
        }
    }

    /*
     * The user has chosen to exit the application
     */
    public void exit() {
        boolean continueTracking = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(GeneralKeys.KEY_SMAP_EXIT_TRACK_MENU, false);
        if(!continueTracking) {
            GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_SMAP_USER_LOCATION, false);
            this.finish();
        } else {
            SnackbarUtils.showLongSnackbar(findViewById(R.id.pager), Collect.getInstance().getString(R.string.smap_continue_tracking));
        }

    }

    @Override
    protected void onPause() {
        mPaused = true;
        super.onPause();

        if (listener != null) {
            try {
                unregisterReceiver(listener);
                listener = null;
            } catch (Exception e) {
                // Ignore - presumably already unregistered
            }
        }

        if (refreshListener != null) {
            try {
                unregisterReceiver(refreshListener);
                refreshListener = null;
            } catch (Exception e) {
                // Ignore - presumably already unregistered
            }
        }
        listenerRegistered = false;
    }

    /*
     * Start of content migration functions
     */

    private void onStorageMigrationFinish(StorageMigrationResult result) {
        if (result == StorageMigrationResult.SUCCESS) {
            DialogUtils.dismissDialog(StorageMigrationDialog.class, getSupportFragmentManager());
            displayBannerWithSuccessStorageMigrationResult();
        } else {
            DialogUtils.dismissDialog(StorageMigrationDialog.class, getSupportFragmentManager());
        }
    }

    @Nullable
    private StorageMigrationDialog showStorageMigrationDialog() {
        Bundle args = new Bundle();
        args.putInt(StorageMigrationDialog.ARG_UNSENT_INSTANCES, Utilities.countFinalised());

        showIfNotShowing(StorageMigrationDialog.class, args, getSupportFragmentManager());
        return getDialog(StorageMigrationDialog.class, getSupportFragmentManager());
    }

    private void setUpStorageMigrationBanner() {

    }

    private void displayBannerWithSuccessStorageMigrationResult() {
        storageMigrationBanner.setVisibility(View.VISIBLE);
        storageMigrationBanner.setText(getString(R.string.storage_migration_completed));
        storageMigrationBanner.setActionText(getString(R.string.scoped_storage_dismiss));
        storageMigrationBanner.setAction(() -> {
            storageMigrationBanner.setVisibility(View.GONE);
            storageMigrationRepository.clearResult();
        });
    }

    private void tryToPerformAutomaticMigration() {
        if (storageStateProvider.shouldPerformAutomaticMigration()) {
            StorageMigrationDialog dialog = showStorageMigrationDialog();
            if (dialog != null) {
                dialog.startStorageMigration();
            }
        }
    }

    protected class RefreshListener extends BroadcastReceiver {

        public RefreshListener (Context context) {
            LocalBroadcastManager.getInstance(context).registerReceiver(this,
                    new IntentFilter("org.smap.smapTask.refresh"));
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            Timber.i("Intent received: %s", intent.getAction());

            model.loadData();
        }
    }
}
