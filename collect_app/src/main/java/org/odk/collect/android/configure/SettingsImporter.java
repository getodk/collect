package org.odk.collect.android.configure;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator;
import org.odk.collect.android.preferences.PreferencesDataSource;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SettingsImporter {

    private final PreferencesDataSource generalPrefs;
    private final PreferencesDataSource adminPrefs;
    private final SettingsPreferenceMigrator preferenceMigrator;
    private final SettingsValidator settingsValidator;
    private final Map<String, Object> generalDefaults;
    private final Map<String, Object> adminDefaults;
    private final SettingsChangeHandler settingsChangedHandler;

    public SettingsImporter(PreferencesDataSource generalPrefs, PreferencesDataSource adminPrefs, SettingsPreferenceMigrator preferenceMigrator, SettingsValidator settingsValidator, Map<String, Object> generalDefaults, Map<String, Object> adminDefaults, SettingsChangeHandler settingsChangedHandler) {
        this.generalPrefs = generalPrefs;
        this.adminPrefs = adminPrefs;
        this.preferenceMigrator = preferenceMigrator;
        this.settingsValidator = settingsValidator;
        this.generalDefaults = generalDefaults;
        this.adminDefaults = adminDefaults;
        this.settingsChangedHandler = settingsChangedHandler;
    }

    public boolean fromJSON(@NonNull String json) {
        if (!settingsValidator.isValid(json)) {
            return false;
        }

        generalPrefs.clear();
        adminPrefs.clear();

        try {
            JSONObject jsonObject = new JSONObject(json);

            JSONObject general = jsonObject.getJSONObject("general");
            importToPrefs(general, generalPrefs);

            JSONObject admin = jsonObject.getJSONObject("admin");
            importToPrefs(admin, adminPrefs);
        } catch (JSONException ignored) {
            // Ignored
        }

        preferenceMigrator.migrate(generalPrefs, adminPrefs);

        clearUnknownKeys(generalPrefs, generalDefaults);
        clearUnknownKeys(adminPrefs, adminDefaults);

        loadDefaults(generalPrefs, generalDefaults);
        loadDefaults(adminPrefs, adminDefaults);

        for (Map.Entry<String, ?> entry: generalPrefs.getAll().entrySet()) {
            settingsChangedHandler.onSettingChanged(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, ?> entry: adminPrefs.getAll().entrySet()) {
            settingsChangedHandler.onSettingChanged(entry.getKey(), entry.getValue());
        }

        return true;
    }



    private void importToPrefs(JSONObject object, PreferencesDataSource preferences) throws JSONException {
        Iterator<String> generalKeys = object.keys();

        while (generalKeys.hasNext()) {
            String key = generalKeys.next();
            preferences.save(key, object.get(key));
        }
    }

    private void loadDefaults(PreferencesDataSource preferences, Map<String, Object> defaults) {
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            if (!preferences.contains(entry.getKey())) {
                preferences.save(entry.getKey(), entry.getValue());
            }
        }
    }

    private void clearUnknownKeys(PreferencesDataSource preferences, Map<String, Object> defaults) {
        Set<String> keys = preferences.getAll().keySet();
        for (String key : keys) {
            if (!defaults.containsKey(key)) {
                preferences.remove(key);
            }
        }
    }
}
