package org.odk.collect.android.application;

import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.preferences.PreferencesProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_CARTO;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_USGS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASEMAP_SOURCE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_CARTO_MAP_STYLE;
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
}
