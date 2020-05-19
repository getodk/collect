package org.odk.collect.android.metadata;

import android.content.SharedPreferences;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import static org.odk.collect.utilities.RandomString.randomString;

public class SharedPreferencesInstallIDProvider implements InstallIDProvider {

    private final SharedPreferences sharedPreferences;
    private final String preferencesKey;

    public SharedPreferencesInstallIDProvider(SharedPreferences sharedPreferences, String preferencesKey) {
        this.sharedPreferences = sharedPreferences;
        this.preferencesKey = preferencesKey;
    }

    @Override
    public String getInstallID() {
        String id = sharedPreferences.getString(preferencesKey, null);  // smap
        if (sharedPreferences.contains(preferencesKey) && (id != null && !id.startsWith("collect:"))) {  // smap Add override as test phones will already have beeen set to collect:  remove this on release after 6.100
            return sharedPreferences.getString(preferencesKey, null);
        } else {
            return generateAndStoreInstallID();
        }
    }

    private String generateAndStoreInstallID() {
        String installID = Collect.getInstance().getApplicationContext().getString(R.string.app_name) + ":" + randomString(16); // smap replace collect wih app name
        sharedPreferences
                .edit()
                .putString(preferencesKey, installID)
                .apply();

        return installID;
    }
}
