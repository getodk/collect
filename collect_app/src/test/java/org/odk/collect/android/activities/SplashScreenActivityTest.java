package org.odk.collect.android.activities;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.support.AlwaysDenyStoragePermissionPermissionUtils;
import org.odk.collect.android.activities.support.AlwaysGrantStoragePermissionsPermissionUtils;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.utilities.PermissionUtils;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class SplashScreenActivityTest {

    @Before
    public void setup() {
        RobolectricHelpers.mountExternalStorage();
    }

    @Test
    public void whenStoragePermissionGranted_createsODKDirectories() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public PermissionUtils providesPermissionUtils() {
                return new AlwaysGrantStoragePermissionsPermissionUtils();
            }
        });

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
}