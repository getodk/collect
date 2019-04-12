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
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.location.Location;       // smap
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobManagerCreateException;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.amazonaws.mobile.AWSMobileClient;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.external.handler.SmapRemoteDataItem; 
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent;
import org.odk.collect.android.jobs.CollectJobCreator;
import org.odk.collect.android.loaders.GeofenceEntry;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.FormInfo;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.AutoSendPreferenceMigrator;
import org.odk.collect.android.taskModel.FormLaunchDetail;
import org.odk.collect.android.taskModel.FormRestartDetails;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.preferences.FormMetadataMigrator;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.tasks.sms.SmsNotificationReceiver;
import org.odk.collect.android.tasks.sms.SmsSentBroadcastReceiver;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.utilities.NotificationUtils;
import org.odk.collect.android.utilities.PRNGFixes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Stack;

import timber.log.Timber;

import static org.odk.collect.android.logic.PropertyManager.PROPMGR_USERNAME;
import static org.odk.collect.android.logic.PropertyManager.SCHEME_USERNAME;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_APP_LANGUAGE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FONT_SIZE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_USERNAME;
import static org.odk.collect.android.tasks.sms.SmsNotificationReceiver.SMS_NOTIFICATION_ACTION;
import static org.odk.collect.android.tasks.sms.SmsSender.SMS_SEND_ACTION;

/**
 * The Open Data Kit Collect application.
 *
 * @author carlhartung
 */
public class Collect extends Application {

    // Storage paths
    public static final String ODK_ROOT = Environment.getExternalStorageDirectory()
            + File.separator + "fieldTask";   // smap
    public static final String FORMS_PATH = ODK_ROOT + File.separator + "forms";
    public static final String INSTANCES_PATH = ODK_ROOT + File.separator + "instances";
    public static final String CACHE_PATH = ODK_ROOT + File.separator + ".cache";
    public static final String METADATA_PATH = ODK_ROOT + File.separator + "metadata";
    public static final String TMPFILE_PATH = CACHE_PATH + File.separator + "tmp.jpg";
    public static final String TMPDRAWFILE_PATH = CACHE_PATH + File.separator + "tmpDraw.jpg";
    public static final String DEFAULT_FONTSIZE = "21";
    public static final int DEFAULT_FONTSIZE_INT = 21;
    public static final String OFFLINE_LAYERS = ODK_ROOT + File.separator + "layers";
    public static final String SETTINGS = ODK_ROOT + File.separator + "settings";

    public static final int CLICK_DEBOUNCE_MS = 1000;

    public static String defaultSysLanguage;
    private static Collect singleton;
    private static long lastClickTime;
    private static String lastClickName;

    @Nullable
    private FormController formController;
    private ExternalDataManager externalDataManager;
    //private Tracker tracker;    // smap
    private AppDependencyComponent applicationComponent;

    private Location location = null;                   // smap
    private ArrayList<GeofenceEntry> geofences = new ArrayList<GeofenceEntry>();    // smap
    private boolean recordLocation = false;             // smap
    private FormInfo formInfo = null;                   // smap
    private boolean tasksDownloading = false;           // smap
    // Keep a reference to form entry activity to allow cancel dialogs to be shown during remote calls
    private FormEntryActivity formEntryActivity = null; // smap
    private HashMap<String, SmapRemoteDataItem> remoteCache = null;         // smap
    private HashMap<String, String> remoteCalls = null;                     // smap
    private Stack<FormLaunchDetail> formStack = new Stack<>();              // smap
    private FormRestartDetails mRestartDetails;                             // smap

    public static Collect getInstance() {
        return singleton;
    }

    public static int getQuestionFontsize() {
        // For testing:
        Collect instance = Collect.getInstance();
        if (instance == null) {
            return Collect.DEFAULT_FONTSIZE_INT;
        }

        return Integer.parseInt(String.valueOf(GeneralSharedPreferences.getInstance().get(KEY_FONT_SIZE)));
    }

    /**
     * Creates required directories on the SDCard (or other external storage)
     *
     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
     */
    public static void createODKDirs() throws RuntimeException {
        String cardstatus = Environment.getExternalStorageState();
        if (!cardstatus.equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException(
                    Collect.getInstance().getString(R.string.sdcard_unmounted, cardstatus));
        }

        String[] dirs = {
                ODK_ROOT, FORMS_PATH, INSTANCES_PATH, CACHE_PATH, METADATA_PATH, OFFLINE_LAYERS
        };

        for (String dirName : dirs) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    String message = getInstance().getString(R.string.cannot_create_directory, dirName);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            } else {
                if (!dir.isDirectory()) {
                    String message = getInstance().getString(R.string.not_a_directory, dirName);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            }
        }
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
        if (dirPath.startsWith(Collect.ODK_ROOT)) {
            dirPath = dirPath.substring(Collect.ODK_ROOT.length());
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

    public String getVersionedAppName() {
        String versionName = BuildConfig.VERSION_NAME;
        versionName = " " + versionName.replaceFirst("-", "\n");
        return getString(R.string.app_name) + versionName;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo currentNetworkInfo = manager.getActiveNetworkInfo();
        return currentNetworkInfo != null && currentNetworkInfo.isConnected();
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

        setupDagger();

        NotificationUtils.createNotificationChannel(singleton);

        registerReceiver(new SmsSentBroadcastReceiver(), new IntentFilter(SMS_SEND_ACTION));
        registerReceiver(new SmsNotificationReceiver(), new IntentFilter(SMS_NOTIFICATION_ACTION));

        try {
            JobManager
                    .create(this)
                    .addJobCreator(new CollectJobCreator());
        } catch (JobManagerCreateException e) {
            Timber.e(e);
        }

        reloadSharedPreferences();

        PRNGFixes.apply();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        JodaTimeAndroid.init(this);

        defaultSysLanguage = Locale.getDefault().getLanguage();
        new LocaleHelper().updateLocale(this);

        FormMetadataMigrator.migrate(PreferenceManager.getDefaultSharedPreferences(this));
        AutoSendPreferenceMigrator.migrate();

        initProperties();

        if (BuildConfig.BUILD_TYPE.equals("release")) {     // smap change from odkCollectRelease
            Timber.plant(new CrashReportingTree());
        } else {
            Timber.plant(new Timber.DebugTree());
        }

        setupLeakCanary();
    }

    private void setupDagger() {
        applicationComponent = DaggerAppDependencyComponent.builder()
                .application(this)
                .build();

        applicationComponent.inject(this);
    }

    protected RefWatcher setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //noinspection deprecation
        defaultSysLanguage = newConfig.locale.getLanguage();
        boolean isUsingSysLanguage = GeneralSharedPreferences.getInstance().get(KEY_APP_LANGUAGE).equals("");
        if (!isUsingSysLanguage) {
            new LocaleHelper().updateLocale(this);
        }
    }

    // Begin Smap
    // start, set and get location
    public void setFormInfo(FormInfo v) {
        formInfo = v;
    }
    public FormInfo getFormInfo() {
        return formInfo;
    }

    public void setLocation(Location l) {
        location = l;
    }
    public Location getLocation() {
        return location;
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
        remoteCalls = new HashMap<String, String>();
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
    public void startRemoteCall(String key) {
        remoteCalls.put(key, "started");
    }
    public void endRemoteCall(String key) {
        remoteCalls.remove(key);
    }
    public String getRemoteCall(String key) {
        return remoteCalls.get(key);
    }
    public boolean inRemoteCall() {
        return remoteCalls.size() > 0;
    }

    public void setFormRestartDetails(FormRestartDetails restartDetails) {
        mRestartDetails = restartDetails;
    }
    public FormRestartDetails getFormRestartDetails() {
        return mRestartDetails;
    }
    // End Smap


    /**
     * Gets the default {@link } for this {@link Application}.
     *
     * @return tracker
     *
     * smap commented out tracker and error reporting functions
    public synchronized Tracker getDefaultTracker() {
        /* smap disable tracker
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.global_tracker);
        }

        return tracker;
    }
    */

    /*
     * smap
     * Push a FormLaunchDetail to the stack
     * this form should then be launched by SmapMain
     */
    public void pushToFormStack(FormLaunchDetail fld) {
        formStack.push(fld);
    }

    /*
     * smap
     * Pop a FormLaunchDetails from the stack
     */
    public FormLaunchDetail popFromFormStack() {
        if(formStack.empty()) {
            return null;
        } else {
            return formStack.pop();
        }
    }

    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return;
            }

            Crashlytics.log(priority, tag, message);

            if (t != null && priority == Log.ERROR) {
                Crashlytics.logException(t);
            }
        }
    }

    public void initProperties() {
        PropertyManager mgr = new PropertyManager(this);

        // Use the server username by default if the metadata username is not defined
        if (mgr.getSingularProperty(PROPMGR_USERNAME) == null || mgr.getSingularProperty(PROPMGR_USERNAME).isEmpty()) {
            mgr.putProperty(PROPMGR_USERNAME, SCHEME_USERNAME, (String) GeneralSharedPreferences.getInstance().get(KEY_USERNAME));
        }

        FormController.initializeJavaRosa(mgr);
    }

    // This method reloads shared preferences in order to load default values for new preferences
    private void reloadSharedPreferences() {
        GeneralSharedPreferences.getInstance().reloadPreferences();
        AdminSharedPreferences.getInstance().reloadPreferences();
    }

    // Debounce multiple clicks within the same screen
    public static boolean allowClick(String className) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        boolean isSameClass = className.equals(lastClickName);
        boolean isBeyondThreshold = elapsedRealtime - lastClickTime > CLICK_DEBOUNCE_MS;
        boolean isBeyondTestThreshold = lastClickTime == 0 || lastClickTime == elapsedRealtime; // just for tests
        boolean allowClick = !isSameClass || isBeyondThreshold || isBeyondTestThreshold;
        if (allowClick) {
            lastClickTime = elapsedRealtime;
            lastClickName = className;
        }
        return allowClick;
    }

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
        String formIdentifier = "";
        FormController formController = getInstance().getFormController();
        if (formController != null) {
            if (formController.getFormDef() != null) {
                String formID = formController.getFormDef().getMainInstance()
                        .getRoot().getAttributeValue("", "id");
                formIdentifier = formController.getFormTitle() + " " + formID;
            }
        }

        return FileUtils.getMd5Hash(
                new ByteArrayInputStream(formIdentifier.getBytes()));
    }

    public void logNullFormControllerEvent(String action) {
        /* smap
        Collect.getInstance().getDefaultTracker()
                .send(new HitBuilders.EventBuilder()
                        .setCategory("NullFormControllerEvent")
                        .setAction(action)
                        .build());
                        */
    }
}
