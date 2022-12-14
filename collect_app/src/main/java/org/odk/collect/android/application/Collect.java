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

import static org.odk.collect.settings.keys.MetaKeys.KEY_GOOGLE_BUG_154855417_FIXED;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.externaldata.ExternalDataManager;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.CollectGeoDependencyModule;
import org.odk.collect.android.injection.config.CollectOsmDroidDependencyModule;
import org.odk.collect.android.injection.config.CollectProjectsDependencyModule;
import org.odk.collect.android.injection.config.CollectSelfieCameraDependencyModule;
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.androidshared.data.AppState;
import org.odk.collect.androidshared.data.StateStore;
import org.odk.collect.androidshared.network.NetworkStateProvider;
import org.odk.collect.androidshared.system.ExternalFilesUtils;
import org.odk.collect.audiorecorder.AudioRecorderDependencyComponent;
import org.odk.collect.audiorecorder.AudioRecorderDependencyComponentProvider;
import org.odk.collect.audiorecorder.DaggerAudioRecorderDependencyComponent;
import org.odk.collect.crashhandler.CrashHandler;
import org.odk.collect.entities.DaggerEntitiesDependencyComponent;
import org.odk.collect.entities.EntitiesDependencyComponent;
import org.odk.collect.entities.EntitiesDependencyComponentProvider;
import org.odk.collect.entities.EntitiesDependencyModule;
import org.odk.collect.entities.EntitiesRepository;
import org.odk.collect.forms.Form;
import org.odk.collect.geo.DaggerGeoDependencyComponent;
import org.odk.collect.geo.GeoDependencyComponent;
import org.odk.collect.geo.GeoDependencyComponentProvider;
import org.odk.collect.maps.layers.ReferenceLayerRepository;
import org.odk.collect.osmdroid.DaggerOsmDroidDependencyComponent;
import org.odk.collect.osmdroid.OsmDroidDependencyComponent;
import org.odk.collect.osmdroid.OsmDroidDependencyComponentProvider;
import org.odk.collect.projects.DaggerProjectsDependencyComponent;
import org.odk.collect.projects.ProjectsDependencyComponent;
import org.odk.collect.projects.ProjectsDependencyComponentProvider;
import org.odk.collect.selfiecamera.DaggerSelfieCameraDependencyComponent;
import org.odk.collect.selfiecamera.SelfieCameraDependencyComponent;
import org.odk.collect.selfiecamera.SelfieCameraDependencyComponentProvider;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.shared.injection.ObjectProvider;
import org.odk.collect.shared.injection.ObjectProviderHost;
import org.odk.collect.shared.injection.SupplierObjectProvider;
import org.odk.collect.shared.settings.Settings;
import org.odk.collect.shared.strings.Md5;
import org.odk.collect.strings.localization.LocalizedApplication;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Locale;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public class Collect extends Application implements
        LocalizedApplication,
        AudioRecorderDependencyComponentProvider,
        ProjectsDependencyComponentProvider,
        GeoDependencyComponentProvider,
        OsmDroidDependencyComponentProvider,
        StateStore,
        ObjectProviderHost,
        EntitiesDependencyComponentProvider,
        SelfieCameraDependencyComponentProvider {

    public static String defaultSysLanguage;
    private static Collect singleton;

    private final AppState appState = new AppState();
    private final SupplierObjectProvider objectProvider = new SupplierObjectProvider();

    private ExternalDataManager externalDataManager;
    private AppDependencyComponent applicationComponent;

    private AudioRecorderDependencyComponent audioRecorderDependencyComponent;
    private ProjectsDependencyComponent projectsDependencyComponent;
    private GeoDependencyComponent geoDependencyComponent;
    private OsmDroidDependencyComponent osmDroidDependencyComponent;
    private EntitiesDependencyComponent entitiesDependencyComponent;
    private SelfieCameraDependencyComponent selfieCameraDependencyComponent;

    /**
     * @deprecated we shouldn't have to reference a static singleton of the application. Code doing this
     * should either have a {@link Context} instance passed to it (or have any references removed if
     * possible).
     */
    @Deprecated
    public static Collect getInstance() {
        return singleton;
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

        CrashHandler.install(this).launchApp(
                () -> ExternalFilesUtils.testExternalFilesAccess(this),
                () -> {
                    setupDagger();
                    DaggerUtils.getComponent(this).inject(this);

                    applicationComponent.applicationInitializer().initialize();
                    fixGoogleBug154855417();
                    setupStrictMode();
                }
        );
    }

    /**
     * Enable StrictMode and log violations to the system log.
     * This catches disk and network access on the main thread, as well as leaked SQLite
     * cursors and unclosed resources.
     */
    private void setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .permitDiskReads()  // shared preferences are being read on main thread
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

    private void setupDagger() {
        applicationComponent = DaggerAppDependencyComponent.builder()
                .application(this)
                .build();

        audioRecorderDependencyComponent = DaggerAudioRecorderDependencyComponent.builder()
                .application(this)
                .build();

        projectsDependencyComponent = DaggerProjectsDependencyComponent.builder()
                .projectsDependencyModule(new CollectProjectsDependencyModule(applicationComponent.projectsRepository()))
                .build();

        selfieCameraDependencyComponent = DaggerSelfieCameraDependencyComponent.builder()
                .selfieCameraDependencyModule(new CollectSelfieCameraDependencyModule(applicationComponent::permissionsChecker))
                .build();

        // Mapbox dependencies
        objectProvider.addSupplier(SettingsProvider.class, applicationComponent::settingsProvider);
        objectProvider.addSupplier(NetworkStateProvider.class, applicationComponent::networkStateProvider);
        objectProvider.addSupplier(ReferenceLayerRepository.class, applicationComponent::referenceLayerRepository);
    }

    @NotNull
    @Override
    public AudioRecorderDependencyComponent getAudioRecorderDependencyComponent() {
        return audioRecorderDependencyComponent;
    }

    @NotNull
    @Override
    public ProjectsDependencyComponent getProjectsDependencyComponent() {
        return projectsDependencyComponent;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //noinspection deprecation
        defaultSysLanguage = newConfig.locale.getLanguage();
    }

    public AppDependencyComponent getComponent() {
        return applicationComponent;
    }

    public void setComponent(AppDependencyComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
        applicationComponent.inject(this);
    }

    /**
     * Gets a unique, privacy-preserving identifier for a form based on its id and version.
     *
     * @param formId      id of a form
     * @param formVersion version of a form
     * @return md5 hash of the form title, a space, the form ID
     */
    public static String getFormIdentifierHash(String formId, String formVersion) {
        Form form = new FormsRepositoryProvider(Collect.getInstance()).get().getLatestByFormIdAndVersion(formId, formVersion);

        String formTitle = form != null ? form.getDisplayName() : "";

        String formIdentifier = formTitle + " " + formId;
        return Md5.getMd5Hash(new ByteArrayInputStream(formIdentifier.getBytes()));
    }

    // https://issuetracker.google.com/issues/154855417
    private void fixGoogleBug154855417() {
        try {
            Settings metaSharedPreferences = applicationComponent.settingsProvider().getMetaSettings();

            boolean hasFixedGoogleBug154855417 = metaSharedPreferences.getBoolean(KEY_GOOGLE_BUG_154855417_FIXED);

            if (!hasFixedGoogleBug154855417) {
                File corruptedZoomTables = new File(getFilesDir(), "ZoomTables.data");
                corruptedZoomTables.delete();

                metaSharedPreferences.save(KEY_GOOGLE_BUG_154855417_FIXED, true);
            }
        } catch (Exception ignored) {
            // ignored
        }
    }

    @NotNull
    @Override
    public Locale getLocale() {
        if (this.applicationComponent != null) {
            return new Locale(LocaleHelper.getLocaleCode(applicationComponent.settingsProvider().getUnprotectedSettings()));
        } else {
            return getResources().getConfiguration().locale;
        }
    }

    @NotNull
    @Override
    public AppState getState() {
        return appState;
    }

    @NonNull
    @Override
    public GeoDependencyComponent getGeoDependencyComponent() {
        if (geoDependencyComponent == null) {
            geoDependencyComponent = DaggerGeoDependencyComponent.builder()
                    .application(this)
                    .geoDependencyModule(new CollectGeoDependencyModule(
                            applicationComponent.mapFragmentFactory(),
                            applicationComponent.locationClient(),
                            applicationComponent.scheduler(),
                            applicationComponent.permissionsChecker()
                    ))
                    .build();
        }

        return geoDependencyComponent;
    }

    @NonNull
    @Override
    public OsmDroidDependencyComponent getOsmDroidDependencyComponent() {
        if (osmDroidDependencyComponent == null) {
            osmDroidDependencyComponent = DaggerOsmDroidDependencyComponent.builder()
                    .osmDroidDependencyModule(new CollectOsmDroidDependencyModule(
                            applicationComponent.referenceLayerRepository(),
                            applicationComponent.locationClient(),
                            applicationComponent.settingsProvider()
                    ))
                    .build();
        }

        return osmDroidDependencyComponent;
    }

    @NonNull
    @Override
    public ObjectProvider getObjectProvider() {
        return objectProvider;
    }

    @NonNull
    @Override
    public EntitiesDependencyComponent getEntitiesDependencyComponent() {
        if (entitiesDependencyComponent == null) {
            entitiesDependencyComponent = DaggerEntitiesDependencyComponent.builder()
                    .entitiesDependencyModule(new EntitiesDependencyModule() {
                        @NonNull
                        @Override
                        public EntitiesRepository providesEntitiesRepository() {
                            String projectId = applicationComponent.currentProjectProvider().getCurrentProject().getUuid();
                            return applicationComponent.entitiesRepositoryProvider().get(projectId);
                        }
                    })
                    .build();
        }

        return entitiesDependencyComponent;
    }

    @NonNull
    @Override
    public SelfieCameraDependencyComponent getSelfieCameraDependencyComponent() {
        return selfieCameraDependencyComponent;
    }
}
