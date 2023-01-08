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

package org.odk.collect.android.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.location.Location;       // smap

import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.amazonaws.mobile.AWSMobileClient;  // smap
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.external.handler.SmapRemoteDataItem;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.loaders.GeofenceEntry;
import org.odk.collect.android.taskModel.FormLaunchDetail;
import org.odk.collect.android.taskModel.FormRestartDetails;
import org.odk.collect.android.application.initialization.ApplicationInitializer;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.androidshared.data.AppState;
import org.odk.collect.androidshared.data.StateStore;
import org.odk.collect.strings.LocalizedApplication;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Locale;

import javax.inject.Inject;

import static org.odk.collect.android.preferences.MetaKeys.KEY_GOOGLE_BUG_154855417_FIXED;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;

import timber.log.Timber;

public class Collect extends Application implements LocalizedApplication,
        StateStore, OnMapsSdkInitializedCallback {
    public static String defaultSysLanguage;
    private static Collect singleton;

    private final AppState appState = new AppState();

    @Nullable
    private FormController formController;
    private ExternalDataManager externalDataManager;
    private AppDependencyComponent applicationComponent;

    private Location location = null;                   // smap
    private Location savedLocation = null;              // Location saved to trail database
    private ArrayList<GeofenceEntry> geofences = new ArrayList<GeofenceEntry>();    // smap
    private boolean tasksDownloading = false;           // smap
    // Keep a reference to form entry activity to allow cancel dialogs to be shown during remote calls
    private FormEntryActivity formEntryActivity = null; // smap
    private HashMap<String, SmapRemoteDataItem> remoteCache = null;         // smap
    private int remoteCalls;                                                // smap
    private Stack<FormLaunchDetail> formStack = new Stack<>();              // smap
    private HashMap<String, String> compoundAddresses = new HashMap<>();
    private FormRestartDetails mRestartDetails;                             // smap
    private String formId;                                                  // smap
    private String searchLocalData;                                         // smap

    @Inject
    ApplicationInitializer applicationInitializer;

    @Inject
    PreferencesProvider preferencesProvider;

    public static Collect getInstance() {
        return singleton;
    }

    /**
     * Predicate that tests whether a directory path might refer to an
     * ODK Tables instance data directory (e.g., for media attachments).
     */
    public static boolean isODKTablesInstanceDataDirectory(File directory) {
        /*
         * Special check to prevent deletion of files that
         * could be in use by ODK Tables.
         */
        String dirPath = directory.getAbsolutePath();
        StoragePathProvider storagePathProvider = new StoragePathProvider();
        if (dirPath.startsWith(storagePathProvider.getStorageRootDirPath())) {
            dirPath = dirPath.substring(storagePathProvider.getStorageRootDirPath().length());
            String[] parts = dirPath.split(File.separatorChar == '\\' ? "\\\\" : File.separator);
            // [appName, instances, tableId, instanceId ]
            if (parts.length == 4 && parts[1].equals("instances")) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public FormController getFormController() {
        return formController;
    }

    public void setFormController(@Nullable FormController controller) {
        formController = controller;
    }

    public ExternalDataManager getExternalDataManager() {
        return externalDataManager;
    }

    public void setExternalDataManager(ExternalDataManager externalDataManager) {
        this.externalDataManager = externalDataManager;
    }

    /*
        Adds support for multidex support library. For more info check out the link below,
        https://developer.android.com/studio/build/multidex.html
    */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, this);

        setupDagger();
        applicationInitializer.initialize();
        
        fixGoogleBug154855417();

        setupStrictMode();
    }

    @Override
    public void onMapsSdkInitialized(MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                Timber.i("The latest version of the renderer is used.");
                break;
            case LEGACY:
                Timber.i("The legacy version of the renderer is used.");
                break;
        }
    }

    /**
     * Enable StrictMode and log violations to the system log.
     * This catches disk and network access on the main thread, as well as leaked SQLite
     * cursors and unclosed resources.
     */
    private void setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    //.detectAll()  // smap
                    .permitDiskReads()  // shared preferences are being read on main thread
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    //.detectAll()  // smap
                    .penaltyLog()
                    .build());
        }
    }

    private void setupDagger() {
        applicationComponent = DaggerAppDependencyComponent.builder()
                .application(this)
                .build();

        applicationComponent.inject(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //noinspection deprecation
        defaultSysLanguage = newConfig.locale.getLanguage();
    }

    // Begin Smap
    public void setFormId(String v) {
        formId = v;
    }
    public String getFormId() {
        return formId;
    }
    public void setSearchLocalData(String v) {
        searchLocalData = v;
    }
    public String getSearchLocalData() {
        return searchLocalData;
    }

    public void setLocation(Location l) {
        location = l;
    }
    public Location getLocation() {
        return location;
    }

    public void setSavedLocation(Location l) {
        savedLocation = l;
    }
    public Location getSavedLocation() {
        return savedLocation;
    }

    public void setGeofences(ArrayList<GeofenceEntry> geofences) {
        this.geofences = geofences;
    }
    public ArrayList<GeofenceEntry> getGeofences() {
        return geofences;
    }

    public void setDownloading(boolean v) {
        tasksDownloading = v;
    }
    public boolean  isDownloading() {
        return tasksDownloading;
    }
    // Initialise AWS
    private void initializeApplication() {
        AWSMobileClient.initializeMobileClientIfNecessary(getApplicationContext());

        // ...Put any application-specific initialization logic here...
    }
    // Set form entry activity
    public void setFormEntryActivity(FormEntryActivity activity) {
        formEntryActivity = activity;
    }
    public FormEntryActivity getFormEntryActivity() {
        return formEntryActivity;
    }
    public void clearRemoteServiceCaches() {
        remoteCache = new HashMap<String, SmapRemoteDataItem>();
    }
    public void initRemoteServiceCaches() {
        if(remoteCache == null) {
            remoteCache = new HashMap<String, SmapRemoteDataItem>();
        } else {
            ArrayList<String> expired = new ArrayList<String>();
            for(String key : remoteCache.keySet()) {
                SmapRemoteDataItem item = remoteCache.get(key);
                if(item.perSubmission) {
                    expired.add(key);

                }
            }
            if(expired.size() > 0) {
                for(String key : expired) {
                    remoteCache.remove(key);
                }
            }
        }
        remoteCalls = 0;
    }
    public String getRemoteData(String key) {
        SmapRemoteDataItem item = remoteCache.get(key);
        if(item != null) {
            return item.data;
        } else {
            return null;
        }
    }
    public void setRemoteItem(SmapRemoteDataItem item) {
        if(item.data == null) {
            // There was a network error
            remoteCache.remove(item.key);
        } else {
            remoteCache.put(item.key, item);
        }
    }
    public void startRemoteCall() {
        remoteCalls++;
    }
    public void endRemoteCall() {
        remoteCalls--;
    }
    public boolean inRemoteCall() {
        return remoteCalls > 0;
    }

    public void setFormRestartDetails(FormRestartDetails restartDetails) {
        mRestartDetails = restartDetails;
    }
    public FormRestartDetails getFormRestartDetails() {
        return mRestartDetails;
    }

    /*
     * Push a FormLaunchDetail to the stack
     * this form should then be launched by SmapMain
     */
    public void pushToFormStack(FormLaunchDetail fld) {
        formStack.push(fld);
    }

    /*
     * Pop a FormLaunchDetails from the stack
     */
    public FormLaunchDetail popFromFormStack() {
        if(formStack.empty()) {
            return null;
        } else {
            return formStack.pop();
        }
    }

    public void clearCompoundAddresses() {
        compoundAddresses = new HashMap<String, String> ();
    }

    public void putCompoundAddress(String qName, String address) {
        compoundAddresses.put(qName, address);
    }

    public String getCompoundAddress(String qName) {
        return compoundAddresses.get(qName);
    }
    // End Smap

    public AppDependencyComponent getComponent() {
        return applicationComponent;
    }

    public void setComponent(AppDependencyComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
        applicationComponent.inject(this);
    }

    /**
     * Gets a unique, privacy-preserving identifier for the current form.
     *
     * @return md5 hash of the form title, a space, the form ID
     */
    public static String getCurrentFormIdentifierHash() {
        FormController formController = getInstance().getFormController();
        if (formController != null) {
            return formController.getCurrentFormIdentifierHash();
        }

        return "";
    }

    /**
     * Gets a unique, privacy-preserving identifier for a form based on its id and version.
     * @param formId id of a form
     * @param formVersion version of a form
     * @return md5 hash of the form title, a space, the form ID
     */
    public static String getFormIdentifierHash(String formId, String formVersion) {
        String formIdentifier = new FormsDao().getFormTitleForFormIdAndFormVersion(formId, formVersion) + " " + formId;
        return FileUtils.getMd5Hash(new ByteArrayInputStream(formIdentifier.getBytes()));
    }

    // https://issuetracker.google.com/issues/154855417
    private void fixGoogleBug154855417() {
        try {
            SharedPreferences metaSharedPreferences = preferencesProvider.getMetaSharedPreferences();

            boolean hasFixedGoogleBug154855417 = metaSharedPreferences.getBoolean(KEY_GOOGLE_BUG_154855417_FIXED, false);

            if (!hasFixedGoogleBug154855417) {
                File corruptedZoomTables = new File(getFilesDir(), "ZoomTables.data");
                corruptedZoomTables.delete();

                metaSharedPreferences
                        .edit()
                        .putBoolean(KEY_GOOGLE_BUG_154855417_FIXED, true)
                        .apply();
            }
        } catch (Exception ignored) {
            // ignored
        }
    }

    @NotNull
    @Override
    public Locale getLocale() {
        return new Locale(LocaleHelper.getLocaleCode(this));
    }

    @NotNull
    @Override
    public AppState getState() {
        return appState;
    }
}
