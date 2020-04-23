package org.odk.collect.android.application.initialization;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobManagerCreateException;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import net.danlew.android.joda.JodaTimeAndroid;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.jobs.CollectJobCreator;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.AutoSendPreferenceMigrator;
import org.odk.collect.android.preferences.FormMetadataMigrator;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.MetaKeys;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.utilities.NotificationUtils;

import java.util.Locale;

import timber.log.Timber;

public class ApplicationInitializer {

    private final Application context;
    private final CollectJobCreator collectJobCreator;
    private final SharedPreferences metaSharedPreferences;

    private boolean firstRun;

    public boolean isFirstRun() {
        return firstRun;
    }

    public ApplicationInitializer(Application context, CollectJobCreator collectJobCreator, SharedPreferences metaSharedPreferences) {
        this.context = context;
        this.collectJobCreator = collectJobCreator;
        this.metaSharedPreferences = metaSharedPreferences;
    }

    public void initializePreferences() {
        performMigrations();
        reloadSharedPreferences();
        setRunningVersion();
    }

    public void initializeFrameworks() {
        NotificationUtils.createNotificationChannel(context);

        try {
            JobManager
                    .create(context)
                    .addJobCreator(collectJobCreator);
        } catch (JobManagerCreateException e) {
            Timber.e(e);
        }

        JodaTimeAndroid.init(context);

        if (BuildConfig.BUILD_TYPE.equals("odkCollectRelease")) {
                Timber.plant(new CrashReportingTree());
        } else {
            Timber.plant(new Timber.DebugTree());
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public void initializeLocale() {
        Collect.defaultSysLanguage = Locale.getDefault().getLanguage();
        new LocaleHelper().updateLocale(context);
    }

    private void setRunningVersion() {
        PackageInfo packageInfo = null;
        try {
            packageInfo =
                    context.getPackageManager().getPackageInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "Unable to get package info");
        }

        firstRun = metaSharedPreferences.getBoolean(MetaKeys.KEY_FIRST_RUN, true);
        metaSharedPreferences.edit().putBoolean(MetaKeys.KEY_FIRST_RUN, false).apply();

        if (metaSharedPreferences.getLong(MetaKeys.KEY_LAST_VERSION, 0) < packageInfo.versionCode) {
            metaSharedPreferences.edit()
                    .putLong(MetaKeys.KEY_LAST_VERSION, packageInfo.versionCode)
                    .apply();

            firstRun = true;
        }
    }

    private void reloadSharedPreferences() {
        GeneralSharedPreferences.getInstance().reloadPreferences();
        AdminSharedPreferences.getInstance().reloadPreferences();
    }

    private void performMigrations() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        FormMetadataMigrator.migrate(prefs);
        PrefMigrator.migrateSharedPrefs();
        AutoSendPreferenceMigrator.migrate();
    }

    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return;
            }

            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.log((priority == Log.ERROR ? "E/" : "W/") + tag + ": " + message);

            if (t != null && priority == Log.ERROR) {
                crashlytics.recordException(t);
            }
        }
    }
}

