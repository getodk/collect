package odk.hedera.collect.application.initialization;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobManagerCreateException;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import net.danlew.android.joda.JodaTimeAndroid;

import org.odk.hedera.collect.BuildConfig;
import odk.hedera.collect.application.Collect;
import odk.hedera.collect.geo.MapboxUtils;
import odk.hedera.collect.jobs.CollectJobCreator;
import odk.hedera.collect.preferences.AdminSharedPreferences;
import odk.hedera.collect.preferences.AutoSendPreferenceMigrator;
import odk.hedera.collect.preferences.GeneralSharedPreferences;
import odk.hedera.collect.utilities.LocaleHelper;
import odk.hedera.collect.utilities.NotificationUtils;
import odk.hedera.collect.utilities.UserAgentProvider;

import java.util.Locale;

import timber.log.Timber;

public class ApplicationInitializer {

    private final Application context;
    private final CollectJobCreator collectJobCreator;
    private final SharedPreferences metaSharedPreferences;
    private final UserAgentProvider userAgentProvider;
    private final GeneralSharedPreferences generalSharedPreferences;
    private final AdminSharedPreferences adminSharedPreferences;

    public ApplicationInitializer(Application context, CollectJobCreator collectJobCreator, SharedPreferences metaSharedPreferences, UserAgentProvider userAgentProvider) {
        this.context = context;
        this.collectJobCreator = collectJobCreator;
        this.metaSharedPreferences = metaSharedPreferences;
        this.userAgentProvider = userAgentProvider;

        generalSharedPreferences = GeneralSharedPreferences.getInstance();
        adminSharedPreferences = AdminSharedPreferences.getInstance();
    }

    public void initializePreferences() {
        performMigrations();
        reloadSharedPreferences();
    }

    public void initializeFrameworks() {
        NotificationUtils.createNotificationChannel(context);
        initializeJobManager();
        JodaTimeAndroid.init(context);
        initializeLogging();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        initializeMapFrameworks();
    }

    public void initializeLocale() {
        Collect.defaultSysLanguage = Locale.getDefault().getLanguage();
        new LocaleHelper().updateLocale(context);
    }

    private void initializeLogging() {
        if (BuildConfig.BUILD_TYPE.equals("odkCollectRelease")) {
            Timber.plant(new CrashReportingTree());
        } else {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void initializeJobManager() {
        try {
            JobManager
                    .create(context)
                    .addJobCreator(collectJobCreator);
        } catch (JobManagerCreateException e) {
            Timber.e(e);
        }
    }

    private void reloadSharedPreferences() {
        generalSharedPreferences.reloadPreferences();
        adminSharedPreferences.reloadPreferences();
    }

    private void performMigrations() {
        new PrefMigrator(
                generalSharedPreferences.getSharedPreferences(),
                adminSharedPreferences.getSharedPreferences(),
                metaSharedPreferences)
                .migrate();

        AutoSendPreferenceMigrator.migrate();
    }

    private void initializeMapFrameworks() {
        new com.google.android.gms.maps.MapView(context).onCreate(null);
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue(userAgentProvider.getUserAgent());
        MapboxUtils.initMapbox();
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

