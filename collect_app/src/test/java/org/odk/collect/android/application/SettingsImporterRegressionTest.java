package org.odk.collect.android.application;

import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.preferences.PreferencesProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_CARTO;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_GOOGLE;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_MAPBOX;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_USGS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASEMAP_SOURCE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_CARTO_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_GOOGLE_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_MAPBOX_MAP_STYLE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_USGS_MAP_STYLE;

@RunWith(AndroidJUnit4.class)
public class SettingsImporterRegressionTest {


    private SettingsImporter settingsImporter;
    private PreferencesProvider preferencesProvider;

    @Before
    public void setup() {
        AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Collect>getApplicationContext());
        settingsImporter = component.settingsImporter();
        preferencesProvider = component.preferencesProvider();
    }

    @Test
    public void cartoDarkMatter() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"osmdroid\",\"map_basemap_behavior\":\"openmap_cartodb_darkmatter\"},\"admin\":{}}");
        SharedPreferences prefs = preferencesProvider.getGeneralSharedPreferences();
        assertThat(prefs.getString(KEY_BASEMAP_SOURCE, null), is(BASEMAP_SOURCE_CARTO));
        assertThat(prefs.getString(KEY_CARTO_MAP_STYLE, null), is("dark_matter"));
    }

    @Test
    public void cartoPositron() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"osmdroid\",\"map_basemap_behavior\":\"openmap_cartodb_positron\"},\"admin\":{}}");
        SharedPreferences prefs = preferencesProvider.getGeneralSharedPreferences();
        assertThat(prefs.getString(KEY_BASEMAP_SOURCE, null), is(BASEMAP_SOURCE_CARTO));
        assertThat(prefs.getString(KEY_CARTO_MAP_STYLE, null), is("positron"));
    }

    @Test
    public void usgsHybrid() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"osmdroid\",\"map_basemap_behavior\":\"openmap_usgs_sat\"},\"admin\":{}}");
        SharedPreferences prefs = preferencesProvider.getGeneralSharedPreferences();
        assertThat(prefs.getString(KEY_BASEMAP_SOURCE, null), is(BASEMAP_SOURCE_USGS));
        assertThat(prefs.getString(KEY_USGS_MAP_STYLE, null), is("hybrid"));
    }

    @Test
    public void googleMapsSatellite() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"google_maps\",\"map_basemap_behavior\":\"satellite\"},\"admin\":{}}");
        SharedPreferences prefs = preferencesProvider.getGeneralSharedPreferences();
        assertThat(prefs.getString(KEY_BASEMAP_SOURCE, null), is(BASEMAP_SOURCE_GOOGLE));
        assertThat(prefs.getString(KEY_GOOGLE_MAP_STYLE, null), is(String.valueOf(GoogleMap.MAP_TYPE_SATELLITE)));
    }

    @Test
    public void mapboxLight() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"mapbox_maps\",\"map_basemap_behavior\":\"mapbox_light\"},\"admin\":{}}");
        SharedPreferences prefs = preferencesProvider.getGeneralSharedPreferences();
        assertThat(prefs.getString(KEY_BASEMAP_SOURCE, null), is(BASEMAP_SOURCE_MAPBOX));
        assertThat(prefs.getString(KEY_MAPBOX_MAP_STYLE, null), is(String.valueOf(Style.LIGHT)));
    }

    @Test
    public void adminPW() {
        settingsImporter.fromJSON("{\"general\":{\"periodic_form_updates_check\":\"every_fifteen_minutes\"},\"admin\":{\"admin_pw\":\"blah\"}}");
        SharedPreferences prefs = preferencesProvider.getAdminSharedPreferences();
        assertThat(prefs.getString(KEY_ADMIN_PW, null), is("blah"));
    }
}
