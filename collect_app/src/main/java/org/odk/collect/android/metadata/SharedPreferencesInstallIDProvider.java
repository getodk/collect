package org.odk.collect.android.metadata;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import static org.odk.collect.utilities.RandomString.randomString;

public class SharedPreferencesInstallIDProvider implements InstallIDProvider {

    private static final String PREFERENCES_KEY = "installid";

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesInstallIDProvider(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public String getInstallID() {
        if (!sharedPreferences.contains(PREFERENCES_KEY)) {
            generateInstallID();
        }

        return sharedPreferences.getString(PREFERENCES_KEY, null);
    }

    @SuppressLint("ApplySharedPref")
    private void generateInstallID() {
        sharedPreferences
                .edit()
                .putString(PREFERENCES_KEY, "collect:" + randomString(16))
                .commit();
    }
}
