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
import android.content.res.Configuration;
import android.os.StrictMode;

import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.initialization.ApplicationInitializer;
import org.odk.collect.android.externaldata.ExternalDataManager;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.androidshared.data.AppState;
import org.odk.collect.androidshared.data.StateStore;
import org.odk.collect.androidshared.utils.ExternalFilesUtils;
import org.odk.collect.audiorecorder.AudioRecorderDependencyComponent;
import org.odk.collect.audiorecorder.AudioRecorderDependencyComponentProvider;
import org.odk.collect.audiorecorder.DaggerAudioRecorderDependencyComponent;
import org.odk.collect.forms.Form;
import org.odk.collect.projects.DaggerProjectsDependencyComponent;
import org.odk.collect.projects.ProjectsDependencyComponent;
import org.odk.collect.projects.ProjectsDependencyComponentProvider;
import org.odk.collect.projects.ProjectsDependencyModule;
import org.odk.collect.projects.ProjectsRepository;
import org.odk.collect.shared.Settings;
import org.odk.collect.shared.strings.Md5;
import org.odk.collect.strings.LocalizedApplication;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Locale;

import javax.inject.Inject;

import static org.odk.collect.android.preferences.keys.MetaKeys.KEY_GOOGLE_BUG_154855417_FIXED;

public class Collect extends Application implements
        LocalizedApplication,
        AudioRecorderDependencyComponentProvider,
        ProjectsDependencyComponentProvider,
        StateStore {
    public static String defaultSysLanguage;
    private static Collect singleton;

    private final AppState appState = new AppState();

    @Nullable
    private FormController formController;
    private ExternalDataManager externalDataManager;
    private AppDependencyComponent applicationComponent;

    @Inject
    ApplicationInitializer applicationInitializer;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    ProjectsRepository projectsRepository;

    private AudioRecorderDependencyComponent audioRecorderDependencyComponent;
    private ProjectsDependencyComponent projectsDependencyComponent;

    /**
     * @deprecated we shouldn't have to reference a static singleton of the application. Code doing this
     * should either have a {@link Context} instance passed to it (or have any references removed if
     * possible).
     */
    @Deprecated
    public static Collect getInstance() {
        return singleton;
    }

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
        ExternalFilesUtils.testExternalFilesAccess(this);

        singleton = this;

        setupDagger();
        applicationInitializer.initialize();

        fixGoogleBug154855417();

        setupStrictMode();
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
        applicationComponent.inject(this);

        audioRecorderDependencyComponent = DaggerAudioRecorderDependencyComponent.builder()
                .application(this)
                .build();

        projectsDependencyComponent = DaggerProjectsDependencyComponent.builder()
                .projectsDependencyModule(new ProjectsDependencyModule() {
                    @NotNull
                    @Override
                    public ProjectsRepository providesProjectsRepository() {
                        return projectsRepository;
                    }
                })
                .build();
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
            Settings metaSharedPreferences = settingsProvider.getMetaSettings();

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
        return new Locale(LocaleHelper.getLocaleCode(settingsProvider.getGeneralSettings()));
    }

    @NotNull
    @Override
    public AppState getState() {
        return appState;
    }
}
