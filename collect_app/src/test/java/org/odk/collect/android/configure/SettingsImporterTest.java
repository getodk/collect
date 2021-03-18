package org.odk.collect.android.configure;

import androidx.core.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator;
import org.odk.collect.android.preferences.source.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Settings generalSettings = TestSettingsProvider.getGeneralSettings();
    private final Settings adminSettings = TestSettingsProvider.getAdminSettings();
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
        settingsValidator = mock(SettingsValidator.class);
        when(settingsValidator.isValid(any())).thenReturn(true);

        importer = new SettingsImporter(generalSettings, adminSettings, (Settings generalSettings, Settings adminSettings) -> {}, settingsValidator, generalDefaults, adminDefaults, (key, newValue) -> {});
    }

    @Test
    public void whenJSONSettingsAreInvalid_returnsFalse() throws Exception {
        when(settingsValidator.isValid(emptySettings())).thenReturn(false);
        assertThat(importer.fromJSON(emptySettings()), is(false));
    }

    @Test
    public void forSettingsKeysNotINJSON_savesDefaults() throws Exception {
        assertThat(importer.fromJSON(emptySettings()), is(true));

        assertPrefs(generalSettings,
                "key1", "default",
                "key2", true
        );
        assertPrefs(adminSettings,
                "key1", 5
        );
    }

    @Test
    public void whenKeysAlreadyExistInPrefs_overridesWithDefaults() throws Exception {
        initPrefs(generalSettings,
                "key1", "existing",
                "key2", false
        );
        initPrefs(adminSettings,
                "key1", 0
        );

        assertThat(importer.fromJSON(emptySettings()), is(true));

        assertPrefs(generalSettings,
                "key1", "default",
                "key2", true
        );
        assertPrefs(adminSettings,
                "key1", 5
        );
    }

    @Test
    public void removesUnknownKeys() throws Exception {
        JSONObject json = emptySettingsObject()
                .put("general", new JSONObject()
                        .put("unknown_key", "value"));

        assertThat(importer.fromJSON(json.toString()), is(true));
        assertThat(generalSettings.contains("unknown_key"), is(false));
    }

    @Test // Migrations might add/rename/move keys
    public void migratesPreferences_beforeLoadingDefaults() throws Exception {
        SettingsPreferenceMigrator migrator = (Settings generalSettings, Settings adminSettings) -> {
            if (this.generalSettings.contains("key1")) {
                throw new RuntimeException("defaults already loaded!");
            }
        };

        importer = new SettingsImporter(generalSettings, adminSettings, migrator, settingsValidator, generalDefaults, adminDefaults, (key, newValue) -> {});
        assertThat(importer.fromJSON(emptySettings()), is(true));
    }

    @Test // Migrations might use old keys that are "unknown" to the app
    public void migratesPreferences_beforeClearingUnknowns() throws Exception {
        JSONObject json = emptySettingsObject()
                .put("general", new JSONObject()
                        .put("unknown_key", "value"));

        SettingsPreferenceMigrator migrator = (Settings generalSettings, Settings adminSettings) -> {
            if (!this.generalSettings.contains("unknown_key")) {
                throw new RuntimeException("unknowns already cleared!");
            }
        };

        importer = new SettingsImporter(generalSettings, adminSettings, migrator, settingsValidator, generalDefaults, adminDefaults, (key, newValue) -> {});
        assertThat(importer.fromJSON(json.toString()), is(true));
    }

    @Test
    public void afterSettingsImportedAndMigrated_runsSettingsChangeHandlerForEveryKey() throws Exception {
        RecordingSettingsChangeHandler handler = new RecordingSettingsChangeHandler();

        importer = new SettingsImporter(generalSettings, adminSettings, (Settings generalSettings, Settings adminSettings) -> {}, settingsValidator, generalDefaults, adminDefaults, handler);
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