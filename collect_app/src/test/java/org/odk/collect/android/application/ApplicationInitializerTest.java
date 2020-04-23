package org.odk.collect.android.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.jobs.CollectJobCreator;
import org.odk.collect.android.preferences.MetaKeys;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class ApplicationInitializerTest {

    private SharedPreferences metaSharedPreferences;
    private ApplicationInitializer applicationInitializer;
    private Application application;

    @Before
    public void setup() {
        application = ApplicationProvider.getApplicationContext();
        metaSharedPreferences = application.getSharedPreferences("blah", Context.MODE_PRIVATE);
        applicationInitializer = new ApplicationInitializer(
                application,
                mock(CollectJobCreator.class),
                metaSharedPreferences
        );
    }

    @Test
    public void initializePreferences_setsVersionInMetaPrefs() throws PackageManager.NameNotFoundException {
        assertThat(metaSharedPreferences.contains(MetaKeys.KEY_LAST_VERSION), is(false));

        applicationInitializer.initializePreferences();
        int versionCode = getCurrentVersionCode();
        assertThat(metaSharedPreferences.getLong(MetaKeys.KEY_LAST_VERSION, 0), is((long) versionCode));
    }

    @Test
    public void initializePreferences_whenThereIsAHigherVersionCodeInPrefs_doesNotSetVersionInMetaPrefs() throws PackageManager.NameNotFoundException {
        int versionCode = getCurrentVersionCode();
        metaSharedPreferences.edit().putLong(MetaKeys.KEY_LAST_VERSION, versionCode + 1).apply();

        applicationInitializer.initializePreferences();
        assertThat(metaSharedPreferences.getLong(MetaKeys.KEY_LAST_VERSION, 0), is((long) versionCode + 1));
    }

    @Test
    public void initializePreferences_whenThereIsALowerVersionCodeInPrefs_setsVersionInMetaPrefs() throws PackageManager.NameNotFoundException {
        int versionCode = getCurrentVersionCode();
        metaSharedPreferences.edit().putLong(MetaKeys.KEY_LAST_VERSION, versionCode - 1).apply();

        applicationInitializer.initializePreferences();
        assertThat(metaSharedPreferences.getLong(MetaKeys.KEY_LAST_VERSION, 0), is((long) versionCode));
    }

    @Test
    public void initializePreferences_setsFirstRunTrue_andFirstRunInPrefsFalse() {
        assertThat(metaSharedPreferences.contains(MetaKeys.KEY_FIRST_RUN), is(false));

        applicationInitializer.initializePreferences();
        assertThat(applicationInitializer.isFirstRun(), is(true));
        assertThat(metaSharedPreferences.getBoolean(MetaKeys.KEY_FIRST_RUN, true), is(false));
    }

    @Test
    public void initializePreferences_whenFirstRunIsFalseInPrefs_andTheVersionCodeInPrefsIsTheSame_setsFirstRunFalse() throws PackageManager.NameNotFoundException {
        int versionCode = getCurrentVersionCode();
        metaSharedPreferences.edit().putLong(MetaKeys.KEY_LAST_VERSION, versionCode).apply();
        metaSharedPreferences.edit().putBoolean(MetaKeys.KEY_FIRST_RUN, false).apply();

        applicationInitializer.initializePreferences();
        assertThat(applicationInitializer.isFirstRun(), is(false));
    }

    @Test
    public void initializePreferences_whenFirstRunIsFalseInPrefs_andThereIsALowerVersionCodeInPrefs_setsFirstRunTrue() throws PackageManager.NameNotFoundException {
        int versionCode = getCurrentVersionCode();
        metaSharedPreferences.edit().putLong(MetaKeys.KEY_LAST_VERSION, versionCode - 1).apply();
        metaSharedPreferences.edit().putBoolean(MetaKeys.KEY_FIRST_RUN, false).apply();

        applicationInitializer.initializePreferences();
        assertThat(applicationInitializer.isFirstRun(), is(true));
    }

    private int getCurrentVersionCode() throws PackageManager.NameNotFoundException {
        return application.getPackageManager().getPackageInfo(application.getPackageName(),
                PackageManager.GET_META_DATA).versionCode;
    }
}