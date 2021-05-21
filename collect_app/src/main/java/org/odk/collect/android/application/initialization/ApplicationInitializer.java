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
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.geo.MapboxUtils;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointActionHandler;
import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.keys.MetaKeys;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.projects.ProjectImporter;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.utilities.AppStateProvider;
import org.odk.collect.projects.Project;
import org.odk.collect.shared.Settings;
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
    private final Settings generalSettings;
    private final Settings adminSettings;
    private final Settings metaSettings;
    private final StorageInitializer storageInitializer;
    private final AppStateProvider appStateProvider;
    private final ProjectImporter projectImporter;
    private final CurrentProjectProvider currentProjectProvider;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public ApplicationInitializer(Application context, UserAgentProvider userAgentProvider, SettingsPreferenceMigrator preferenceMigrator,
                                  PropertyManager propertyManager, Analytics analytics, StorageInitializer storageInitializer, Settings generalSettings,
                                  Settings adminSettings, Settings metaSettings, AppStateProvider appStateProvider, ProjectImporter projectImporter, CurrentProjectProvider currentProjectProvider) {
        this.context = context;
        this.userAgentProvider = userAgentProvider;
        this.preferenceMigrator = preferenceMigrator;
        this.propertyManager = propertyManager;
        this.analytics = analytics;
        this.generalSettings = generalSettings;
        this.adminSettings = adminSettings;
        this.metaSettings = metaSettings;
        this.storageInitializer = storageInitializer;
        this.appStateProvider = appStateProvider;
        this.projectImporter = projectImporter;
        this.currentProjectProvider = currentProjectProvider;
    }

    public void initialize() {
        initializeStorage();
        initializePreferences();
        initializeFrameworks();
        initializeLocale();
        importExistingProjectIfNeeded();
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
        boolean isAnalyticsEnabled = generalSettings.getBoolean(GeneralKeys.KEY_ANALYTICS);
        analytics.setAnalyticsCollectionEnabled(isAnalyticsEnabled);

        FormUpdateMode formUpdateMode = getFormUpdateMode(context, generalSettings);
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
        generalSettings.setDefaultForAllSettingsWithoutValues();
        adminSettings.setDefaultForAllSettingsWithoutValues();
    }

    private void performMigrations() {
        preferenceMigrator.migrate(generalSettings, adminSettings);
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

    private void importExistingProjectIfNeeded() {
        if (!appStateProvider.isFreshInstall() && !metaSettings.getBoolean(MetaKeys.EXISTING_PROJECT_IMPORTED)) {
            Project.Saved project = projectImporter.importExistingProject();
            currentProjectProvider.setCurrentProject(project.getUuid());
        }

        metaSettings.save(MetaKeys.EXISTING_PROJECT_IMPORTED, true);
    }
}

