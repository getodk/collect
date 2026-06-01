package org.odk.collect.android.application;

import static android.os.Environment.MEDIA_MOUNTED;
import static org.robolectric.Shadows.shadowOf;
import static java.util.Arrays.asList;

import android.content.Context;
import android.os.StrictMode;

import androidx.preference.Preference;
import androidx.test.core.app.ApplicationProvider;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.geo.MapConfiguratorProvider;
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard;
import org.odk.collect.crashhandler.CrashHandler;
import org.odk.collect.db.sqlite.DatabaseConnection;
import org.odk.collect.maps.MapConfigurator;
import org.odk.collect.shared.settings.Settings;
import org.odk.collect.strings.R.string;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowEnvironment;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author James Knight
 */

public class RobolectricApplication extends Collect implements CollectComposeThemeProvider {

    @Override
    public void onCreate() {
        // Make sure storage is accessible
        ShadowEnvironment.setExternalStorageState(MEDIA_MOUNTED);

        // Prevents OKHttp from exploding on initialization https://github.com/robolectric/robolectric/issues/5115
        System.setProperty("javax.net.ssl.trustStore", "NONE");

        // We need this so WorkManager.getInstance doesn't explode
        try {
            WorkManager.initialize(ApplicationProvider.getApplicationContext(), new Configuration.Builder().build());
        } catch (IllegalStateException e) {
            // initialize() explodes if it's already been called
        }

        // We don't want to deal with permission checks in Robolectric
        ShadowApplication shadowApplication = shadowOf(this);
        shadowApplication.grantPermissions("android.permission.ACCESS_FINE_LOCATION");
        shadowApplication.grantPermissions("android.permission.ACCESS_COARSE_LOCATION");
        shadowApplication.grantPermissions("android.permission.READ_EXTERNAL_STORAGE");
        shadowApplication.grantPermissions("android.permission.CAMERA");
        shadowApplication.grantPermissions("android.permission.RECORD_AUDIO");
        shadowApplication.grantPermissions("android.permission.GET_ACCOUNTS");

        // These clear static state that can't persist from test to test
        DatabaseConnection.closeAll();

        // We don't want any clicks to be blocked
        MultiClickGuard.test = true;

        CrashHandler.uninstall(this);

        super.onCreate();

        // Don't enforce strict mode as we always use the "main" thread in Robolectric
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .permitCustomSlowCalls()
                .build()
        );

        MapConfiguratorProvider.initOptions(asList(new MapConfiguratorProvider.SourceOption("fake-map", string.maps, new MapConfigurator() {
            @Override
            public boolean isAvailable(@NotNull Context context) {
                return true;
            }

            @Override
            public void showUnavailableMessage(@NotNull Context context) {

            }

            @Override
            public @NotNull List<@NotNull Preference> createPrefs(@NotNull Context context, @NotNull Settings settings) {
                return Collections.emptyList();
            }

            @Override
            public boolean supportsLayer(@NotNull File file) {
                return true;
            }

            @Override
            public @NotNull String getDisplayName(@NotNull File file) {
                return "";
            }
        })));
    }
}
