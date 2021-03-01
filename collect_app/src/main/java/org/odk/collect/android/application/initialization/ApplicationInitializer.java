package org.odk.collect.android.application.initialization;

import android.app.Application;
import android.os.Handler;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.startup.AppInitializer;

import net.danlew.android.joda.JodaTimeInitializer;

import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.parse.XFormParser;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.geo.MapboxUtils;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointActionHandler;
import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesDataSource;
import org.odk.collect.android.preferences.PreferencesRepository;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.utilities.UserAgentProvider;

import java.util.Locale;

import timber.log.Timber;

import static org.odk.collect.android.configure.SettingsUtils.getFormUpdateMode;

public class ApplicationInitializer {

    private final Application context;
    private final UserAgentProvider userAgentProvider;
    private final SettingsPreferenceMigrator preferenceMigrator;
    private final PropertyManager propertyManager;
    private final Analytics analytics;
    private final PreferencesDataSource generalPrefs;
    private final PreferencesDataSource adminPrefs;
    private final StorageInitializer storageInitializer;

    public ApplicationInitializer(Application context, UserAgentProvider userAgentProvider, SettingsPreferenceMigrator preferenceMigrator,
                                  PropertyManager propertyManager, Analytics analytics, StorageInitializer storageInitializer, PreferencesRepository preferencesRepository) {
        this.context = context;
        this.userAgentProvider = userAgentProvider;
        this.preferenceMigrator = preferenceMigrator;
        this.propertyManager = propertyManager;
        this.analytics = analytics;
        this.storageInitializer = storageInitializer;

        generalPrefs = preferencesRepository.getGeneralPreferences();
        adminPrefs = preferencesRepository.getAdminPreferences();
    }

    public void initialize() {
        initializeStorage();
        initializePreferences();
        initializeFrameworks();
        initializeLocale();
    }

    private void initializeStorage() {
        storageInitializer.createOdkDirsOnStorage();
    }

    private void initializePreferences() {
        performMigrations();
        reloadSharedPreferences();
    }

    private void initializeFrameworks() {
        initializeLogging();
        AppInitializer.getInstance(context).initializeComponent(JodaTimeInitializer.class);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        initializeMapFrameworks();
        initializeJavaRosa();
        initializeAnalytics();
    }

    private void initializeAnalytics() {
        boolean isAnalyticsEnabled = generalPrefs.getBoolean(GeneralKeys.KEY_ANALYTICS);
        analytics.setAnalyticsCollectionEnabled(isAnalyticsEnabled);

        FormUpdateMode formUpdateMode = getFormUpdateMode(context, generalPrefs);
        analytics.setUserProperty("FormUpdateMode", formUpdateMode.getValue(context));
    }

    private void initializeLocale() {
        Collect.defaultSysLanguage = Locale.getDefault().getLanguage();
    }

    private void initializeJavaRosa() {
        propertyManager.reload();
        org.javarosa.core.services.PropertyManager
                .setPropertyManager(propertyManager);

        // Register prototypes for classes that FormDef uses
        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames);
        PrototypeManager.registerPrototypes(CoreModelModule.classNames);
        new XFormsModule().registerModule();

        // When registering prototypes from Collect, a proguard exception also needs to be added
        PrototypeManager.registerPrototype("org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointAction");
        XFormParser.registerActionHandler(CollectSetGeopointActionHandler.ELEMENT_NAME, new CollectSetGeopointActionHandler());
    }

    private void initializeLogging() {
        if (BuildConfig.BUILD_TYPE.equals("odkCollectRelease")) {
            Timber.plant(new CrashReportingTree(analytics));
        } else {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void reloadSharedPreferences() {
        generalPrefs.loadDefaultPreferences();
        adminPrefs.loadDefaultPreferences();
    }

    private void performMigrations() {
        preferenceMigrator.migrate(generalPrefs, adminPrefs);
    }

    private void initializeMapFrameworks() {
        try {
            Handler handler = new Handler(context.getMainLooper());
            handler.post(() -> {
                // This has to happen on the main thread but we might call `initialize` from tests
                new com.google.android.gms.maps.MapView(context).onCreate(null);
            });
            org.osmdroid.config.Configuration.getInstance().setUserAgentValue(userAgentProvider.getUserAgent());
            MapboxUtils.initMapbox();
        } catch (Exception | Error ignore) {
            // ignored
        }
    }
}

