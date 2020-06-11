package org.odk.collect.android.configure;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.application.initialization.migration.PreferenceMigrator;

import java.util.Iterator;

public class CollectSettingsImporter {

    private final SharedPreferences generalSharedPrefs;
    private final SharedPreferences adminSharedPrefs;
    private final PreferenceMigrator preferenceMigrator;

    public CollectSettingsImporter(SharedPreferences generalSharedPrefs, SharedPreferences adminSharedPrefs, PreferenceMigrator preferenceMigrator) {
        this.generalSharedPrefs = generalSharedPrefs;
        this.adminSharedPrefs = adminSharedPrefs;
        this.preferenceMigrator = preferenceMigrator;
    }

    public boolean fromJSON(@NonNull String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);

            JSONObject general = jsonObject.getJSONObject("general");
            saveToPrefs(general, generalSharedPrefs);

            JSONObject admin = jsonObject.getJSONObject("admin");
            saveToPrefs(admin, adminSharedPrefs);
        } catch (JSONException ignored) {
            // Ignored
        }

        preferenceMigrator.migrate();
        return true;
    }

    private void saveToPrefs(JSONObject general, SharedPreferences generalSharedPrefs) throws JSONException {
        Iterator<String> generalKeys = general.keys();

        while (generalKeys.hasNext()) {
            String key = generalKeys.next();
            generalSharedPrefs.edit().putString(key, general.getString(key)).apply();
        }
    }
}
