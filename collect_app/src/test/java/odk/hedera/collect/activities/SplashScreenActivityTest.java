package odk.hedera.collect.activities;

import android.app.Application;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.hedera.collect.R;
import odk.hedera.collect.activities.support.AlwaysDenyStoragePermissionPermissionUtils;
import odk.hedera.collect.activities.support.AlwaysGrantStoragePermissionsPermissionUtils;
import odk.hedera.collect.application.initialization.ApplicationInitializer;
import odk.hedera.collect.injection.config.AppDependencyModule;
import odk.hedera.collect.jobs.CollectJobCreator;
import odk.hedera.collect.preferences.GeneralKeys;
import odk.hedera.collect.preferences.GeneralSharedPreferences;
import odk.hedera.collect.preferences.PreferencesProvider;
import odk.hedera.collect.storage.StoragePathProvider;
import org.odk.hedera.collect.support.RobolectricHelpers;
import odk.hedera.collect.utilities.PermissionUtils;
import odk.hedera.collect.utilities.UserAgentProvider;
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
            public PermissionUtils providesPermissionUtils() {
                return new AlwaysGrantStoragePermissionsPermissionUtils();
            }

            @Override
            public ApplicationInitializer providesApplicationInitializer(Application application, CollectJobCreator collectJobCreator, PreferencesProvider preferencesProvider, UserAgentProvider userAgentProvider) {
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
    }

    @Test
    public void whenStoragePermissionIsNotGranted_finishes() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public PermissionUtils providesPermissionUtils() {
                return new AlwaysDenyStoragePermissionPermissionUtils();
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