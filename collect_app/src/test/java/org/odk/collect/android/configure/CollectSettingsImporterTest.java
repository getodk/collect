package org.odk.collect.android.configure;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.initialization.migration.PreferenceMigrator;
import org.odk.collect.android.javarosawrapper.JavaRosaInitializer;

import java.util.HashMap;
import java.util.Map;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("PMD.DoubleBraceInitialization")
public class CollectSettingsImporterTest {

    private SharedPreferences generalPrefs;
    private SharedPreferences adminPrefs;
    private SettingsValidator settingsValidator;
    private CollectSettingsImporter importer;

    private final Map<String, Object> generalDefaults = new HashMap<String, Object>() {{
        put("key1", "default");
        put("key2", true);
    }};

    private final Map<String, Object> adminDefaults = new HashMap<String, Object>() {{
        put("key1", 5);
    }};

    @Before
    public void setup() {
        generalPrefs = getApplicationContext().getSharedPreferences("test1", Context.MODE_PRIVATE);
        adminPrefs = getApplicationContext().getSharedPreferences("test2", Context.MODE_PRIVATE);

        settingsValidator = mock(SettingsValidator.class);
        when(settingsValidator.isValid(any())).thenReturn(true);

        importer = new CollectSettingsImporter(generalPrefs, adminPrefs, () -> {}, settingsValidator, generalDefaults, adminDefaults, () -> { });
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
        PreferenceMigrator migrator = () -> {
            if (generalPrefs.contains("key1")) {
                throw new RuntimeException("defaults already loaded!");
            }
        };

        importer = new CollectSettingsImporter(generalPrefs, adminPrefs, migrator, settingsValidator, generalDefaults, adminDefaults, () -> { });
        assertThat(importer.fromJSON(emptySettings()), is(true));
    }

    @Test // Migrations might use old keys that are "unknown" to the app
    public void migratesPreferences_beforeClearingUnknowns() throws Exception {
        JSONObject json = emptySettingsObject()
                .put("general", new JSONObject()
                        .put("unknown_key", "value"));

        PreferenceMigrator migrator = () -> {
            if (!generalPrefs.contains("unknown_key")) {
                throw new RuntimeException("unknowns already cleared!");
            }
        };

        importer = new CollectSettingsImporter(generalPrefs, adminPrefs, migrator, settingsValidator, generalDefaults, adminDefaults, () -> { });
        assertThat(importer.fromJSON(json.toString()), is(true));
    }

    @Test
    public void afterSettingsImportedAndMigrated_initializesJavaRosa() throws Exception {
        final String[] key1ValueWhenCalled = {null};
        JavaRosaInitializer javaRosaInitializer = () -> {
            key1ValueWhenCalled[0] = generalPrefs.getString("key1", null);
        };

        importer = new CollectSettingsImporter(generalPrefs, adminPrefs, () -> {}, settingsValidator, generalDefaults, adminDefaults, javaRosaInitializer);
        assertThat(importer.fromJSON(emptySettings()), is(true));
        assertThat(key1ValueWhenCalled[0], is("default"));
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
}