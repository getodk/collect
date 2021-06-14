package org.odk.collect.android.configure;

import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator;
import org.odk.collect.android.configure.qr.AppConfigurationKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.ProjectImporter;
import org.odk.collect.projects.Project;
import org.odk.collect.projects.ProjectsRepository;
import org.odk.collect.shared.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;
import static org.odk.collect.android.injection.DaggerUtils.getComponent;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("PMD.DoubleBraceInitialization")
public class SettingsImporterTest {

    private final SettingsProvider settingsProvider = TestSettingsProvider.getSettingsProvider();
    private final ProjectsRepository projectsRepository = mock(ProjectsRepository.class);
    private SettingsValidator settingsValidator;
    private SettingsImporter importer;
    private String currentProjectId;

    private final Map<String, Object> generalDefaults = new HashMap<String, Object>() {{
        put("key1", "default");
        put("key2", true);
    }};

    private final Map<String, Object> adminDefaults = new HashMap<String, Object>() {{
        put("key1", 5);
    }};

    @Before
    public void setup() {
        getComponent(ApplicationProvider.<Collect>getApplicationContext()).projectImporter().importDemoProject();
        getComponent(ApplicationProvider.<Collect>getApplicationContext()).currentProjectProvider().setCurrentProject(ProjectImporter.DEMO_PROJECT_ID);
        currentProjectId = getComponent(ApplicationProvider.<Collect>getApplicationContext()).currentProjectProvider().getCurrentProject().getUuid();

        settingsValidator = mock(SettingsValidator.class);
        when(settingsValidator.isValid(any())).thenReturn(true);

        importer = new SettingsImporter(settingsProvider, (Settings generalSettings, Settings adminSettings) -> {}, settingsValidator, generalDefaults, adminDefaults, (projectId, key, newValue) -> {}, projectsRepository);
    }

    @Test
    public void whenJSONSettingsAreInvalid_returnsFalse() throws Exception {
        when(settingsValidator.isValid(emptySettings())).thenReturn(false);
        assertThat(importer.fromJSON(emptySettings(), currentProjectId), is(false));
    }

    @Test
    public void forSettingsKeysNotINJSON_savesDefaults() throws Exception {
        assertThat(importer.fromJSON(emptySettings(), currentProjectId), is(true));

        assertPrefs(settingsProvider.getGeneralSettings(),
                "key1", "default",
                "key2", true
        );
        assertPrefs(settingsProvider.getAdminSettings(),
                "key1", 5
        );
    }

    @Test
    public void whenKeysAlreadyExistInPrefs_overridesWithDefaults() throws Exception {
        initPrefs(settingsProvider.getGeneralSettings(),
                "key1", "existing",
                "key2", false
        );
        initPrefs(settingsProvider.getAdminSettings(),
                "key1", 0
        );

        assertThat(importer.fromJSON(emptySettings(), currentProjectId), is(true));

        assertPrefs(settingsProvider.getGeneralSettings(),
                "key1", "default",
                "key2", true
        );
        assertPrefs(settingsProvider.getAdminSettings(),
                "key1", 5
        );
    }

    @Test
    public void removesUnknownKeys() throws Exception {
        JSONObject json = emptySettingsObject()
                .put(AppConfigurationKeys.GENERAL, new JSONObject()
                        .put("unknown_key", "value"));

        assertThat(importer.fromJSON(json.toString(), currentProjectId), is(true));
        assertThat(settingsProvider.getGeneralSettings().contains("unknown_key"), is(false));
    }

    @Test // Migrations might add/rename/move keys
    public void migratesPreferences_beforeLoadingDefaults() throws Exception {
        SettingsPreferenceMigrator migrator = (Settings generalSettings, Settings adminSettings) -> {
            if (settingsProvider.getGeneralSettings().contains("key1")) {
                throw new RuntimeException("defaults already loaded!");
            }
        };

        importer = new SettingsImporter(settingsProvider, migrator, settingsValidator, generalDefaults, adminDefaults, (projectId, key, newValue) -> {}, projectsRepository);
        assertThat(importer.fromJSON(emptySettings(), currentProjectId), is(true));
    }

    @Test // Migrations might use old keys that are "unknown" to the app
    public void migratesPreferences_beforeClearingUnknowns() throws Exception {
        JSONObject json = emptySettingsObject()
                .put(AppConfigurationKeys.GENERAL, new JSONObject()
                        .put("unknown_key", "value"));

        SettingsPreferenceMigrator migrator = (Settings generalSettings, Settings adminSettings) -> {
            if (!settingsProvider.getGeneralSettings().contains("unknown_key")) {
                throw new RuntimeException("unknowns already cleared!");
            }
        };

        importer = new SettingsImporter(settingsProvider, migrator, settingsValidator, generalDefaults, adminDefaults, (projectId, key, newValue) -> {}, projectsRepository);
        assertThat(importer.fromJSON(json.toString(), currentProjectId), is(true));
    }

    @Test
    public void afterSettingsImportedAndMigrated_runsSettingsChangeHandlerForEveryKey() throws Exception {
        RecordingSettingsChangeHandler handler = new RecordingSettingsChangeHandler();

        importer = new SettingsImporter(settingsProvider, (Settings generalSettings, Settings adminSettings) -> {}, settingsValidator, generalDefaults, adminDefaults, handler, projectsRepository);
        assertThat(importer.fromJSON(emptySettings(), currentProjectId), is(true));
        assertThat(handler.changes, containsInAnyOrder(
                new Pair<>("key1", "default"),
                new Pair<>("key2", true),
                new Pair<>("key1", 5)));
    }

    @Test
    public void projectDetailsShouldBeImportedIfIncludedInJson() throws Exception {
        JSONObject projectJson = new JSONObject()
                .put(AppConfigurationKeys.PROJECT_NAME, "Project X")
                .put(AppConfigurationKeys.PROJECT_ICON, "X")
                .put(AppConfigurationKeys.PROJECT_COLOR, "#cccccc");

        JSONObject settings = new JSONObject()
                .put(AppConfigurationKeys.GENERAL, new JSONObject())
                .put(AppConfigurationKeys.ADMIN, new JSONObject())
                .put(AppConfigurationKeys.PROJECT, projectJson);

        when(projectsRepository.get("1")).thenReturn(new Project.Saved("1", "Project Y", "Y", "#ffffff"));

        importer = new SettingsImporter(settingsProvider, (Settings generalSettings, Settings adminSettings) -> {}, settingsValidator, generalDefaults, adminDefaults, (projectId, key, newValue) -> {}, projectsRepository);
        importer.fromJSON(settings.toString(), "1");

        verify(projectsRepository).save(new Project.Saved("1", "Project X", "X", "#cccccc"));
    }

    private String emptySettings() throws Exception {
        return emptySettingsObject()
                .toString();
    }

    private JSONObject emptySettingsObject() throws Exception {
        return new JSONObject()
                .put(AppConfigurationKeys.GENERAL, new JSONObject())
                .put(AppConfigurationKeys.ADMIN, new JSONObject());
    }

    private static class RecordingSettingsChangeHandler implements SettingsChangeHandler {

        public List<Pair<String, Object>> changes = new ArrayList<>();

        @Override
        public void onSettingChanged(String projectId, Object newValue, String changedKey) {
            changes.add(new Pair<>(changedKey, newValue));
        }
    }
}
