package org.odk.collect.android.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.injection.DaggerUtils.getComponent;
import static org.odk.collect.android.preferences.keys.ProjectKeys.BASEMAP_SOURCE_CARTO;
import static org.odk.collect.android.preferences.keys.ProjectKeys.BASEMAP_SOURCE_GOOGLE;
import static org.odk.collect.android.preferences.keys.ProjectKeys.BASEMAP_SOURCE_MAPBOX;
import static org.odk.collect.android.preferences.keys.ProjectKeys.BASEMAP_SOURCE_USGS;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_BASEMAP_SOURCE;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_CARTO_MAP_STYLE;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_GOOGLE_MAP_STYLE;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_MAPBOX_MAP_STYLE;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_USGS_MAP_STYLE;
import static org.odk.collect.android.preferences.keys.ProtectedProjectKeys.KEY_ADMIN_PW;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.projects.Project;
import org.odk.collect.shared.Settings;

@RunWith(AndroidJUnit4.class)
public class SettingsImporterRegressionTest {

    private SettingsImporter settingsImporter;
    private Project.Saved currentProject;
    private final SettingsProvider settingsProvider = TestSettingsProvider.getSettingsProvider();

    @Before
    public void setup() {
        CollectHelpers.createProject(Project.Companion.getDEMO_PROJECT());
        getComponent(ApplicationProvider.<Collect>getApplicationContext()).currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID);
        settingsImporter = getComponent(ApplicationProvider.<Collect>getApplicationContext()).settingsImporter();
        currentProject = getComponent(ApplicationProvider.<Collect>getApplicationContext()).currentProjectProvider().getCurrentProject();
    }

    @Test
    public void cartoDarkMatter() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"osmdroid\",\"map_basemap_behavior\":\"openmap_cartodb_darkmatter\"},\"admin\":{}}", currentProject);
        Settings prefs = settingsProvider.getUnprotectedSettings();
        assertThat(prefs.getString(KEY_BASEMAP_SOURCE), is(BASEMAP_SOURCE_CARTO));
        assertThat(prefs.getString(KEY_CARTO_MAP_STYLE), is("dark_matter"));
    }

    @Test
    public void cartoPositron() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"osmdroid\",\"map_basemap_behavior\":\"openmap_cartodb_positron\"},\"admin\":{}}", currentProject);
        Settings prefs = settingsProvider.getUnprotectedSettings();
        assertThat(prefs.getString(KEY_BASEMAP_SOURCE), is(BASEMAP_SOURCE_CARTO));
        assertThat(prefs.getString(KEY_CARTO_MAP_STYLE), is("positron"));
    }

    @Test
    public void usgsHybrid() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"osmdroid\",\"map_basemap_behavior\":\"openmap_usgs_sat\"},\"admin\":{}}", currentProject);
        Settings prefs = settingsProvider.getUnprotectedSettings();
        assertThat(prefs.getString(KEY_BASEMAP_SOURCE), is(BASEMAP_SOURCE_USGS));
        assertThat(prefs.getString(KEY_USGS_MAP_STYLE), is("hybrid"));
    }

    @Test
    public void googleMapsSatellite() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"google_maps\",\"map_basemap_behavior\":\"satellite\"},\"admin\":{}}", currentProject);
        Settings prefs = settingsProvider.getUnprotectedSettings();
        assertThat(prefs.getString(KEY_BASEMAP_SOURCE), is(BASEMAP_SOURCE_GOOGLE));
        assertThat(prefs.getString(KEY_GOOGLE_MAP_STYLE), is(String.valueOf(GoogleMap.MAP_TYPE_SATELLITE)));
    }

    @Test
    public void mapboxLight() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"mapbox_maps\",\"map_basemap_behavior\":\"mapbox_light\"},\"admin\":{}}", currentProject);
        Settings prefs = settingsProvider.getUnprotectedSettings();
        assertThat(prefs.getString(KEY_BASEMAP_SOURCE), is(BASEMAP_SOURCE_MAPBOX));
        assertThat(prefs.getString(KEY_MAPBOX_MAP_STYLE), is(Style.LIGHT));
    }

    @Test
    public void adminPW() {
        settingsImporter.fromJSON("{\"general\":{\"periodic_form_updates_check\":\"every_fifteen_minutes\"},\"admin\":{\"admin_pw\":\"blah\"}}", currentProject);
        assertThat(settingsProvider.getProtectedSettings().getString(KEY_ADMIN_PW), is("blah"));
    }
}
