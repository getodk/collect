package org.odk.collect.android.activities;

import android.app.Application;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.support.AlwaysDenyStoragePermissionPermissionsProvider;
import org.odk.collect.android.activities.support.AlwaysGrantStoragePermissionsPermissionsProvider;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.application.AppStateProvider;
import org.odk.collect.android.application.initialization.ApplicationInitializer;
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.utilities.UserAgentProvider;
import org.robolectric.annotation.LooperMode;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class SplashScreenActivityTest {

    private ApplicationInitializer applicationInitializer;
    private GeneralSharedPreferences generalSharedPreferences;

    @Before
    public void setup() {
        generalSharedPreferences = GeneralSharedPreferences.getInstance();
        applicationInitializer = mock(ApplicationInitializer.class);

        RobolectricHelpers.mountExternalStorage();
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker, StorageStateProvider storageStateProvider) {
                return new AlwaysGrantStoragePermissionsPermissionsProvider(permissionsChecker, storageStateProvider);
            }

            @Override
            public ApplicationInitializer providesApplicationInitializer(Application application, UserAgentProvider userAgentProvider, SettingsPreferenceMigrator preferenceMigrator, PropertyManager propertyManager, Analytics analytics, AppStateProvider appStateProvider, StorageStateProvider storageStateProvider) {
                return applicationInitializer;
            }
        });
    }

    @Test
    public void whenStoragePermissionGranted_createsODKDirectories() {
        ActivityScenario.launch(SplashScreenActivity.class);

        for (String dirName : new StoragePathProvider().getOdkDirPaths()) {
            File dir = new File(dirName);
            Assert.assertTrue("File " + dirName + "does not exist", dir.exists());
            Assert.assertTrue("File" + dirName + "does not exist", dir.isDirectory());
        }

        assertThat(new StorageStateProvider().isScopedStorageUsed(), is(true));
    }

    @Test
    public void whenStoragePermissionIsNotGranted_finishes() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker, StorageStateProvider storageStateProvider) {
                return new AlwaysDenyStoragePermissionPermissionsProvider(permissionsChecker, storageStateProvider);
            }
        });

        ActivityScenario<SplashScreenActivity> scenario = ActivityScenario.launch(SplashScreenActivity.class);
        assertThat(scenario.getState(), is(Lifecycle.State.DESTROYED));
    }

    @Test
    public void whenShowSplashScreenEnabled_showSplashScreen() {
        generalSharedPreferences.getSharedPreferences()
                .edit()
                .putBoolean(GeneralKeys.KEY_SHOW_SPLASH, true)
                .apply();

        ActivityScenario<SplashScreenActivity> scenario1 = ActivityScenario.launch(SplashScreenActivity.class);
        assertThat(scenario1.getState(), is(Lifecycle.State.RESUMED));
        scenario1.onActivity(activity -> {
            assertThat(activity.findViewById(R.id.splash_default).getVisibility(), is(View.VISIBLE));
        });
    }
}