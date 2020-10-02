package org.odk.collect.android.configure;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("PMD.DoubleBraceInitialization")
public class SettingsImporterTest {

    private SharedPreferences generalPrefs;
    private SharedPreferences adminPrefs;
    private SettingsValidator settingsValidator;
    private SettingsImporter importer;

    private final Map<String, Object> generalDefaults = new HashMap<String, Object>() {{
        put("key1", "default");
        put("key2", true);
    }};

    private final Map<String, Object> adminDefaults = new HashMap<String, Object>() {{
        put("key1", 5);
    }};

    @Before
    public void setup() {
        Context context = getApplicationContext();

        generalPrefs = context.getSharedPreferences("test1", Context.MODE_PRIVATE);
        adminPrefs = context.getSharedPreferences("test2", Context.MODE_PRIVATE);

        settingsValidator = mock(SettingsValidator.class);
        when(settingsValidator.isValid(any())).thenReturn(true);

        importer = new SettingsImporter(generalPrefs, adminPrefs, (SharedPreferences generalSharedPreferences, SharedPreferences adminSharedPreferences) -> {}, settingsValidator, generalDefaults, adminDefaults, (key, newValue) -> {});
    }

    @Test
    public void whenJSONSettingsAreInvalid_returnsFalse() throws Exception {
        when(settingsValidator.isValid(emptySettings())).thenReturn(false);
        assertThat(importer.fromJSON(emptySettings()), is(false));
    }

    @Test
    public void forSettingsKeysNotINJSON_savesDefaults() throws Exception {
        assertThat(importer.fromJSON(emptySettings()), is(true));

        assertPrefs(generalPrefs,
                "key1", "default",
                "key2", true
        );
        assertPrefs(adminPrefs,
                "key1", 5
        );
    }

    @Test
    public void whenKeysAlreadyExistInPrefs_overridesWithDefaults() throws Exception {
        initPrefs(generalPrefs,
                "key1", "existing",
                "key2", false
        );
        initPrefs(adminPrefs,
                "key1", 0
        );

        assertThat(importer.fromJSON(emptySettings()), is(true));

        assertPrefs(generalPrefs,
                "key1", "default",
                "key2", true
        );
        assertPrefs(adminPrefs,
                "key1", 5
        );
    }

    @Test
    public void removesUnknownKeys() throws Exception {
        JSONObject json = emptySettingsObject()
                .put("general", new JSONObject()
                        .put("unknown_key", "value"));

        assertThat(importer.fromJSON(json.toString()), is(true));
        assertThat(generalPrefs.contains("unknown_key"), is(false));
    }

    @Test // Migrations might add/rename/move keys
    public void migratesPreferences_beforeLoadingDefaults() throws Exception {
        SettingsPreferenceMigrator migrator = (SharedPreferences generalSharedPreferences, SharedPreferences adminSharedPreferences) -> {
            if (generalPrefs.contains("key1")) {
                throw new RuntimeException("defaults already loaded!");
            }
        };

        importer = new SettingsImporter(generalPrefs, adminPrefs, migrator, settingsValidator, generalDefaults, adminDefaults, (key, newValue) -> {});
        assertThat(importer.fromJSON(emptySettings()), is(true));
    }

    @Test // Migrations might use old keys that are "unknown" to the app
    public void migratesPreferences_beforeClearingUnknowns() throws Exception {
        JSONObject json = emptySettingsObject()
                .put("general", new JSONObject()
                        .put("unknown_key", "value"));

        SettingsPreferenceMigrator migrator = (SharedPreferences generalSharedPreferences, SharedPreferences adminSharedPreferences) -> {
            if (!generalPrefs.contains("unknown_key")) {
                throw new RuntimeException("unknowns already cleared!");
            }
        };

        importer = new SettingsImporter(generalPrefs, adminPrefs, migrator, settingsValidator, generalDefaults, adminDefaults, (key, newValue) -> {});
        assertThat(importer.fromJSON(json.toString()), is(true));
    }

    @Test
    public void afterSettingsImportedAndMigrated_runsSettingsChangeHandlerForEveryKey() throws Exception {
        RecordingSettingsChangeHandler handler = new RecordingSettingsChangeHandler();

        importer = new SettingsImporter(generalPrefs, adminPrefs, (SharedPreferences generalSharedPreferences, SharedPreferences adminSharedPreferences) -> {}, settingsValidator, generalDefaults, adminDefaults, handler);
        assertThat(importer.fromJSON(emptySettings()), is(true));
        assertThat(handler.changes, containsInAnyOrder(
                new Pair<>("key1", "default"),
                new Pair<>("key2", true),
                new Pair<>("key1", 5)));
    }

    private String emptySettings() throws Exception {
        return emptySettingsObject()
                .toString();
    }

    private JSONObject emptySettingsObject() throws Exception {
        return new JSONObject()
                .put("general", new JSONObject())
                .put("admin", new JSONObject());
    }

    private static class RecordingSettingsChangeHandler implements SettingsChangeHandler {

        public List<Pair<String, Object>> changes = new ArrayList<>();

        @Override
        public void onSettingChanged(String changedKey, Object newValue) {
            changes.add(new Pair<>(changedKey, newValue));
        }
    }
}