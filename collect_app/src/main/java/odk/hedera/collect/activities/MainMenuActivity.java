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

package odk.hedera.collect.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;

import org.odk.hedera.collect.R;
import odk.hedera.collect.activities.viewmodels.MainMenuViewModel;
import odk.hedera.collect.analytics.Analytics;
import odk.hedera.collect.analytics.AnalyticsEvents;
import odk.hedera.collect.application.Collect;
import odk.hedera.collect.dao.InstancesDao;
import odk.hedera.collect.injection.DaggerUtils;
import odk.hedera.collect.network.NetworkStateProvider;
import odk.hedera.collect.preferences.PreferencesProvider;
import org.odk.hedera.material.MaterialBanner;
import odk.hedera.collect.preferences.AdminKeys;
import odk.hedera.collect.preferences.AdminPasswordDialogFragment;
import odk.hedera.collect.preferences.AdminPasswordDialogFragment.Action;
import odk.hedera.collect.preferences.AdminPreferencesActivity;
import odk.hedera.collect.preferences.AdminSharedPreferences;
import odk.hedera.collect.preferences.AutoSendPreferenceMigrator;
import odk.hedera.collect.preferences.GeneralKeys;
import odk.hedera.collect.preferences.GeneralSharedPreferences;
import odk.hedera.collect.preferences.PreferenceSaver;
import odk.hedera.collect.preferences.PreferencesActivity;
import odk.hedera.collect.preferences.qr.QRCodeTabsActivity;
import odk.hedera.collect.provider.InstanceProviderAPI.InstanceColumns;
import odk.hedera.collect.storage.StorageInitializer;
import odk.hedera.collect.storage.StoragePathProvider;
import odk.hedera.collect.storage.StorageStateProvider;
import odk.hedera.collect.storage.migration.StorageMigrationDialog;
import odk.hedera.collect.storage.migration.StorageMigrationRepository;
import odk.hedera.collect.storage.migration.StorageMigrationResult;
import odk.hedera.collect.utilities.AdminPasswordProvider;
import odk.hedera.collect.utilities.ApplicationConstants;
import odk.hedera.collect.utilities.DialogUtils;
import odk.hedera.collect.utilities.MultiClickGuard;
import odk.hedera.collect.utilities.PlayServicesChecker;
import odk.hedera.collect.utilities.SharedPreferencesUtils;
import odk.hedera.collect.utilities.ToastUtils;
import odk.hedera.collect.version.VersionInformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static odk.hedera.collect.preferences.MetaKeys.KEY_MAPBOX_INITIALIZED;
import static odk.hedera.collect.utilities.DialogUtils.getDialog;
import static odk.hedera.collect.utilities.DialogUtils.showIfNotShowing;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends CollectAbstractActivity implements AdminPasswordDialogFragment.AdminPasswordDialogCallback {
    private static final boolean EXIT = true;
    // buttons
    private Button manageFilesButton;
    private Button sendDataButton;
    private Button viewSentFormsButton;
    private Button reviewDataButton;
    private Button getFormsButton;
    private AlertDialog alertDialog;
    private MenuItem qrcodeScannerMenuItem;
    private int completedCount;
    private int savedCount;
    private int viewSentCount;
    private Cursor finalizedCursor;
    private Cursor savedCursor;
    private Cursor viewSentCursor;
    private final IncomingHandler handler = new IncomingHandler(this);
    private final MyContentObserver contentObserver = new MyContentObserver();

    @Inject
    public Analytics analytics;

    @BindView(R.id.storageMigrationBanner)
    MaterialBanner storageMigrationBanner;

    @BindView(R.id.version_sha)
    TextView versionSHAView;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    @Inject
    StorageStateProvider storageStateProvider;

    @Inject
    StoragePathProvider storagePathProvider;

    @Inject
    AdminPasswordProvider adminPasswordProvider;

    @Inject
    VersionInformation versionInformation;

    @Inject
    NetworkStateProvider connectivityProvider;

    @Inject
    GeneralSharedPreferences generalSharedPreferences;

    @Inject
    PreferencesProvider preferencesProvider;

    private MainMenuViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Collect.getInstance().getComponent().inject(this);
        setContentView(R.layout.main_menu);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this, new MainMenuViewModel.Factory(versionInformation)).get(MainMenuViewModel.class);

        initToolbar();
        initMapBox();
        DaggerUtils.getComponent(this).inject(this);

        storageMigrationRepository.getResult().observe(this, this::onStorageMigrationFinish);

        // enter data button. expects a result.
        Button enterDataButton = findViewById(R.id.enter_data);
        enterDataButton.setText(getString(R.string.enter_data_button));
        enterDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(),
                            FormChooserListActivity.class);
                    startActivity(i);
                }
            }
        });

        // review data button. expects a result.
        reviewDataButton = findViewById(R.id.review_data);
        reviewDataButton.setText(getString(R.string.review_data_button));
        reviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                    i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                            ApplicationConstants.FormModes.EDIT_SAVED);
                    startActivity(i);
                }
            }
        });

        // send data button. expects a result.
        sendDataButton = findViewById(R.id.send_data);
        sendDataButton.setText(getString(R.string.send_data_button));
        sendDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(),
                            InstanceUploaderListActivity.class);
                    startActivity(i);
                }
            }
        });

        //View sent forms
        viewSentFormsButton = findViewById(R.id.view_sent_forms);
        viewSentFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                    i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                            ApplicationConstants.FormModes.VIEW_SENT);
                    startActivity(i);
                }
            }
        });

        // manage forms button. no result expected.
        getFormsButton = findViewById(R.id.get_forms);
        getFormsButton.setText(getString(R.string.get_forms));
        getFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    SharedPreferences sharedPreferences = PreferenceManager
                            .getDefaultSharedPreferences(MainMenuActivity.this);
                    String protocol = sharedPreferences.getString(
                            GeneralKeys.KEY_PROTOCOL, getString(R.string.protocol_odk_default));
                    Intent i = null;
                    if (protocol.equalsIgnoreCase(getString(R.string.protocol_google_sheets))) {
                        if (new PlayServicesChecker().isGooglePlayServicesAvailable(MainMenuActivity.this)) {
                            i = new Intent(getApplicationContext(),
                                    GoogleDriveActivity.class);
                        } else {
                            new PlayServicesChecker().showGooglePlayServicesAvailabilityErrorDialog(MainMenuActivity.this);
                            return;
                        }
                    } else {
                        i = new Intent(getApplicationContext(),
                                FormDownloadListActivity.class);
                    }
                    startActivity(i);
                }
            }
        });

        // manage forms button. no result expected.
        manageFilesButton = findViewById(R.id.manage_forms);
        manageFilesButton.setText(getString(R.string.manage_files));
        manageFilesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(),
                            FileManagerTabs.class);
                    startActivity(i);
                }
            }
        });

        String versionSHA = viewModel.getVersionCommitDescription();
        if (versionSHA != null) {
            versionSHAView.setText(versionSHA);
        } else {
            versionSHAView.setVisibility(View.GONE);
        }

        // must be at the beginning of any activity that can be called from an
        // external intent
        Timber.i("Starting up, creating directories");
        try {
            new StorageInitializer().createOdkDirsOnStorage();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        File f = new File(storagePathProvider.getStorageRootDirPath() + "/collect.settings");
        File j = new File(storagePathProvider.getStorageRootDirPath() + "/collect.settings.json");
        // Give JSON file preference
        if (j.exists()) {
            boolean success = SharedPreferencesUtils.loadSharedPreferencesFromJSONFile(j);
            if (success) {
                ToastUtils.showLongToast(R.string.settings_successfully_loaded_file_notification);
                j.delete();
                recreate();

                // Delete settings file to prevent overwrite of settings from JSON file on next startup
                if (f.exists()) {
                    f.delete();
                }
            } else {
                ToastUtils.showLongToast(R.string.corrupt_settings_file_notification);
            }
        } else if (f.exists()) {
            boolean success = loadSharedPreferencesFromFile(f);
            if (success) {
                ToastUtils.showLongToast(R.string.settings_successfully_loaded_file_notification);
                f.delete();
                recreate();
            } else {
                ToastUtils.showLongToast(R.string.corrupt_settings_file_notification);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        countSavedForms();
        updateButtons();
        if (!storageMigrationRepository.isMigrationBeingPerformed()) {
            getContentResolver().registerContentObserver(InstanceColumns.CONTENT_URI, true, contentObserver);
        }

        setButtonsVisibility();
        invalidateOptionsMenu();
        setUpStorageMigrationBanner();
    }

    private void setButtonsVisibility() {
        reviewDataButton.setVisibility(viewModel.shouldEditSavedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        sendDataButton.setVisibility(viewModel.shouldSendFinalizedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        viewSentFormsButton.setVisibility(viewModel.shouldViewSentFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        getFormsButton.setVisibility(viewModel.shouldGetBlankFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        manageFilesButton.setVisibility(viewModel.shouldDeleteSavedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        getContentResolver().unregisterContentObserver(contentObserver);
    }

    @Override
    public void onDestroy() {
        storageMigrationRepository.clearResult();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        qrcodeScannerMenuItem = menu.findItem(R.id.menu_configure_qr_code);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        qrcodeScannerMenuItem.setVisible(this.getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0).getBoolean(AdminKeys.KEY_QR_CODE_SCANNER, true));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_configure_qr_code:
                analytics.logEvent(AnalyticsEvents.SCAN_QR_CODE, "MainMenu");

                if (adminPasswordProvider.isAdminPasswordSet()) {
                    Bundle args = new Bundle();
                    args.putSerializable(AdminPasswordDialogFragment.ARG_ACTION, Action.SCAN_QR_CODE);
                    showIfNotShowing(AdminPasswordDialogFragment.class, args, getSupportFragmentManager());
                } else {
                    startActivity(new Intent(this, QRCodeTabsActivity.class));
                }
                return true;
            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.menu_general_preferences:
                startActivity(new Intent(this, PreferencesActivity.class));
                return true;
            case R.id.menu_admin_preferences:
                if (adminPasswordProvider.isAdminPasswordSet()) {
                    Bundle args = new Bundle();
                    args.putSerializable(AdminPasswordDialogFragment.ARG_ACTION, Action.ADMIN_SETTINGS);
                    showIfNotShowing(AdminPasswordDialogFragment.class, args, getSupportFragmentManager());
                } else {
                    startActivity(new Intent(this, AdminPreferencesActivity.class));
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMapBox() {
        SharedPreferences metaSharedPreferences = preferencesProvider.getMetaSharedPreferences();
        if (!metaSharedPreferences.getBoolean(KEY_MAPBOX_INITIALIZED, false) && connectivityProvider.isDeviceOnline()) {
            // This "one weird trick" lets us initialize MapBox at app start when the internet is
            // most likely to be available. This is annoyingly needed for offline tiles to work.
            try {
                MapView mapView = new MapView(this);
                FrameLayout mapboxContainer = findViewById(R.id.mapbox_container);
                mapboxContainer.addView(mapView);
                mapView.getMapAsync(mapBoxMap -> mapBoxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                    metaSharedPreferences.edit().putBoolean(KEY_MAPBOX_INITIALIZED, true).apply();
                }));
            } catch (Exception | Error ignored) {
                // This will crash on devices where the arch for MapBox is not included
            }
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(String.format("%s %s", getString(R.string.app_name), viewModel.getVersion()));
        setSupportActionBar(toolbar);
    }

    private void countSavedForms() {
        InstancesDao instancesDao = new InstancesDao();

        // count for finalized instances
        try {
            finalizedCursor = instancesDao.getFinalizedInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        if (finalizedCursor != null) {
            startManagingCursor(finalizedCursor);
        }
        completedCount = finalizedCursor != null ? finalizedCursor.getCount() : 0;

        // count for saved instances
        try {
            savedCursor = instancesDao.getUnsentInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        if (savedCursor != null) {
            startManagingCursor(savedCursor);
        }
        savedCount = savedCursor != null ? savedCursor.getCount() : 0;

        //count for view sent form
        try {
            viewSentCursor = instancesDao.getSentInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }
        if (viewSentCursor != null) {
            startManagingCursor(viewSentCursor);
        }
        viewSentCount = viewSentCursor != null ? viewSentCursor.getCount() : 0;
    }

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), errorListener);
        alertDialog.show();
    }

    private void updateButtons() {
        if (finalizedCursor != null && !finalizedCursor.isClosed()) {
            finalizedCursor.requery();
            completedCount = finalizedCursor.getCount();
            if (completedCount > 0) {
                sendDataButton.setText(
                        getString(R.string.send_data_button, String.valueOf(completedCount)));
            } else {
                sendDataButton.setText(getString(R.string.send_data));
            }
        } else {
            sendDataButton.setText(getString(R.string.send_data));
            Timber.w("Cannot update \"Send Finalized\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }

        if (savedCursor != null && !savedCursor.isClosed()) {
            savedCursor.requery();
            savedCount = savedCursor.getCount();
            if (savedCount > 0) {
                reviewDataButton.setText(getString(R.string.review_data_button,
                        String.valueOf(savedCount)));
            } else {
                reviewDataButton.setText(getString(R.string.review_data));
            }
        } else {
            reviewDataButton.setText(getString(R.string.review_data));
            Timber.w("Cannot update \"Edit Form\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }

        if (viewSentCursor != null && !viewSentCursor.isClosed()) {
            viewSentCursor.requery();
            viewSentCount = viewSentCursor.getCount();
            if (viewSentCount > 0) {
                viewSentFormsButton.setText(
                        getString(R.string.view_sent_forms_button, String.valueOf(viewSentCount)));
            } else {
                viewSentFormsButton.setText(getString(R.string.view_sent_forms));
            }
        } else {
            viewSentFormsButton.setText(getString(R.string.view_sent_forms));
            Timber.w("Cannot update \"View Sent\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }
    }

    private boolean loadSharedPreferencesFromFile(File src) {
        // this should probably be in a thread if it ever gets big
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));

            // first object is preferences
            Map<String, Object> entries = (Map<String, Object>) input.readObject();

            AutoSendPreferenceMigrator.migrate(entries);
            PreferenceSaver.saveGeneralPrefs(generalSharedPreferences, entries);

            // second object is admin options
            Map<String, Object> adminEntries = (Map<String, Object>) input.readObject();
            PreferenceSaver.saveAdminPrefs(AdminSharedPreferences.getInstance(), adminEntries);

            Collect.getInstance().initializeJavaRosa();
            res = true;
        } catch (IOException | ClassNotFoundException e) {
            Timber.e(e, "Exception while loading preferences from file due to : %s ", e.getMessage());
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                Timber.e(ex, "Exception thrown while closing an input stream due to: %s ", ex.getMessage());
            }
        }
        return res;
    }

    @Override
    public void onCorrectAdminPassword(Action action) {
        switch (action) {
            case ADMIN_SETTINGS:
                startActivity(new Intent(this, AdminPreferencesActivity.class));
                break;
            case STORAGE_MIGRATION:
                StorageMigrationDialog dialog = showStorageMigrationDialog();
                if (dialog != null) {
                    dialog.startStorageMigration();
                }

                break;
            case SCAN_QR_CODE:
                startActivity(new Intent(this, QRCodeTabsActivity.class));
                break;
        }
    }

    @Override
    public void onIncorrectAdminPassword() {
        ToastUtils.showShortToast(R.string.admin_password_incorrect);
    }

    /*
     * Used to prevent memory leaks
     */
    static class IncomingHandler extends Handler {
        private final WeakReference<MainMenuActivity> target;

        IncomingHandler(MainMenuActivity target) {
            this.target = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            MainMenuActivity target = this.target.get();
            if (target != null) {
                target.updateButtons();
            }
        }
    }

    /**
     * notifies us that something changed
     */
    private class MyContentObserver extends ContentObserver {

        MyContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            handler.sendEmptyMessage(0);
        }
    }

    private void onStorageMigrationFinish(StorageMigrationResult result) {
        if (result == StorageMigrationResult.SUCCESS) {
            DialogUtils.dismissDialog(StorageMigrationDialog.class, getSupportFragmentManager());
            displayBannerWithSuccessStorageMigrationResult();
        } else {
            StorageMigrationDialog dialog = showStorageMigrationDialog();

            if (dialog != null) {
                dialog.handleMigrationError(result);
            }
        }
    }

    @Nullable
    private StorageMigrationDialog showStorageMigrationDialog() {
        Bundle args = new Bundle();
        args.putInt(StorageMigrationDialog.ARG_UNSENT_INSTANCES, savedCount);

        showIfNotShowing(StorageMigrationDialog.class, args, getSupportFragmentManager());
        return getDialog(StorageMigrationDialog.class, getSupportFragmentManager());
    }

    private void setUpStorageMigrationBanner() {
        if (!storageStateProvider.isScopedStorageUsed()) {
            displayStorageMigrationBanner();
        }
    }

    private void displayStorageMigrationBanner() {
        storageMigrationBanner.setVisibility(View.VISIBLE);
        storageMigrationBanner.setText(getString(R.string.scoped_storage_banner_text));
        storageMigrationBanner.setActionText(getString(R.string.scoped_storage_learn_more));
        storageMigrationBanner.setAction(() -> {
            showStorageMigrationDialog();
            getContentResolver().unregisterContentObserver(contentObserver);
        });
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
}
