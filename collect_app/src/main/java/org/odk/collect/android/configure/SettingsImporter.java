package org.odk.collect.android.configure;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.odk.collect.android.utilities.SharedPreferencesUtils.put;

public class SettingsImporter {

    private final SharedPreferences generalSharedPrefs;
    private final SharedPreferences adminSharedPrefs;
    private final SettingsPreferenceMigrator preferenceMigrator;
    private final SettingsValidator settingsValidator;
    private final Map<String, Object> generalDefaults;
    private final Map<String, Object> adminDefaults;
    private final SettingsChangeHandler settingsChangedHandler;

    public SettingsImporter(SharedPreferences generalSharedPrefs, SharedPreferences adminSharedPrefs, SettingsPreferenceMigrator preferenceMigrator, SettingsValidator settingsValidator, Map<String, Object> generalDefaults, Map<String, Object> adminDefaults, SettingsChangeHandler settingsChangedHandler) {
        this.generalSharedPrefs = generalSharedPrefs;
        this.adminSharedPrefs = adminSharedPrefs;
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

        generalSharedPrefs.edit().clear().apply();
        adminSharedPrefs.edit().clear().apply();

        try {
            JSONObject jsonObject = new JSONObject(json);

            JSONObject general = jsonObject.getJSONObject("general");
            importToPrefs(general, generalSharedPrefs);

            JSONObject admin = jsonObject.getJSONObject("admin");
            importToPrefs(admin, adminSharedPrefs);
        } catch (JSONException ignored) {
            // Ignored
        }

        preferenceMigrator.migrate(generalSharedPrefs, adminSharedPrefs);

        clearUnknownKeys(generalSharedPrefs, generalDefaults);
        clearUnknownKeys(adminSharedPrefs, adminDefaults);

        loadDefaults(generalSharedPrefs, generalDefaults);
        loadDefaults(adminSharedPrefs, adminDefaults);

        for (String key: generalSharedPrefs.getAll().keySet()) {
            settingsChangedHandler.onSettingChanged(key);
        }

        for (String key: adminSharedPrefs.getAll().keySet()) {
            settingsChangedHandler.onSettingChanged(key);
        }

        return true;
    }



    private void importToPrefs(JSONObject object, SharedPreferences sharedPreferences) throws JSONException {
        Iterator<String> generalKeys = object.keys();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        while (generalKeys.hasNext()) {
            String key = generalKeys.next();
            put(editor, key, object.get(key));
        }

        editor.apply();
    }

    private void loadDefaults(SharedPreferences sharedPreferences, Map<String, Object> defaults) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            if (!sharedPreferences.contains(entry.getKey())) {
                put(editor, entry.getKey(), entry.getValue());
            }
        }

        editor.apply();
    }

    private void clearUnknownKeys(SharedPreferences sharedPreferences, Map<String, Object> defaults) {
        Set<String> keys = sharedPreferences.getAll().keySet();
        for (String key : keys) {
            if (!defaults.containsKey(key)) {
                sharedPreferences.edit().remove(key).apply();
            }
        }
    }
}
