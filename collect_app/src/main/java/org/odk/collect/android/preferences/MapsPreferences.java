/*
 * Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.preference.ListPreference;
import android.view.View;

import com.google.common.collect.ObjectArrays;

import org.odk.collect.android.R;
import org.odk.collect.android.map.MapboxUtils;
import org.odk.collect.android.spatial.MapHelper;

import androidx.annotation.Nullable;

import static org.odk.collect.android.preferences.GeneralKeys.GOOGLE_MAPS_BASEMAP_DEFAULT;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_MAP_BASEMAP;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_MAP_SDK;
import static org.odk.collect.android.preferences.GeneralKeys.MAPBOX_BASEMAP_DEFAULT;
import static org.odk.collect.android.preferences.GeneralKeys.MAPBOX_BASEMAP_KEY;
import static org.odk.collect.android.preferences.GeneralKeys.OSM_BASEMAP_KEY;
import static org.odk.collect.android.preferences.GeneralKeys.OSM_MAPS_BASEMAP_DEFAULT;
import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class MapsPreferences extends BasePreferenceFragment {

    public static MapsPreferences newInstance(boolean adminMode) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(INTENT_KEY_ADMIN_MODE, adminMode);

        MapsPreferences prefs = new MapsPreferences();
        prefs.setArguments(bundle);
        return prefs;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.maps_preferences);
        initMapPrefs();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setTitle(R.string.maps);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (toolbar != null) {
            toolbar.setTitle(R.string.general_preferences);
        }
    }

    private void initMapPrefs() {
        final ListPreference mapSdk = (ListPreference) findPreference(KEY_MAP_SDK);
        final ListPreference mapBasemap = (ListPreference) findPreference(KEY_MAP_BASEMAP);

        if (mapSdk == null || mapBasemap == null) {
            return;
        }

        if (mapSdk.getValue() == null || mapSdk.getEntry() == null) {
            mapSdk.setValueIndex(0);  // use the first option as the default
        }

        String[] basemapValues;
        String[] basemapEntries;
        if (mapSdk.getValue().equals(OSM_BASEMAP_KEY)) {
            basemapValues = getResources().getStringArray(R.array.map_osm_basemap_selector_entry_values);
            basemapEntries = getResources().getStringArray(R.array.map_osm_basemap_selector_entries);
        } else if (mapSdk.getValue().equals(MAPBOX_BASEMAP_KEY)) {
            basemapValues = getResources().getStringArray(R.array.map_mapbox_basemap_selector_entry_values);
            basemapEntries = getResources().getStringArray(R.array.map_mapbox_basemap_selector_entries);
        } else { // otherwise fall back to Google, the default
            basemapValues = getResources().getStringArray(R.array.map_google_basemap_selector_entry_values);
            basemapEntries = getResources().getStringArray(R.array.map_google_basemap_selector_entries);
        }
        mapBasemap.setEntryValues(ObjectArrays.concat(basemapValues, MapHelper.getOfflineLayerListWithTags(), String.class));
        mapBasemap.setEntries(ObjectArrays.concat(basemapEntries, MapHelper.getOfflineLayerListWithTags(), String.class));

        mapSdk.setSummary(mapSdk.getEntry());
        mapSdk.setOnPreferenceChangeListener((preference, newValue) -> {
            String value = (String) newValue;
            String[] values;
            String[] entries;

            if (value.equals(OSM_BASEMAP_KEY)) {
                values = getResources().getStringArray(R.array.map_osm_basemap_selector_entry_values);
                entries = getResources().getStringArray(R.array.map_osm_basemap_selector_entries);
                mapBasemap.setValue(OSM_MAPS_BASEMAP_DEFAULT);
            } else if (value.equals(MAPBOX_BASEMAP_KEY)) {
                if (MapboxUtils.initMapbox() == null) {
                    // This settings code will be rewritten very soon (planned for r1.23),
                    // so let's just warn for now instead of trying to disable the option.
                    MapboxUtils.warnMapboxUnsupported(getActivity());
                }
                values = getResources().getStringArray(R.array.map_mapbox_basemap_selector_entry_values);
                entries = getResources().getStringArray(R.array.map_mapbox_basemap_selector_entries);
                mapBasemap.setValue(MAPBOX_BASEMAP_DEFAULT);
            } else {  // GOOGLE_MAPS_BASEMAP_KEY, or default
                values = getResources().getStringArray(R.array.map_google_basemap_selector_entry_values);
                entries = getResources().getStringArray(R.array.map_google_basemap_selector_entries);
                mapBasemap.setValue(GOOGLE_MAPS_BASEMAP_DEFAULT);
            }
            mapBasemap.setEntryValues(ObjectArrays.concat(values, MapHelper.getOfflineLayerListWithTags(), String.class));
            mapBasemap.setEntries(ObjectArrays.concat(entries, MapHelper.getOfflineLayerListWithTags(), String.class));
            mapBasemap.setSummary(mapBasemap.getEntry());

            mapSdk.setSummary(mapSdk.getEntries()[mapSdk.findIndexOfValue(value)]);
            return true;
        });

        CharSequence entry = mapBasemap.getEntry();
        if (entry != null) {
            mapBasemap.setSummary(entry);
        } else {
            mapBasemap.setSummary(mapBasemap.getEntries()[0]);
            mapBasemap.setValueIndex(0);
        }

        mapBasemap.setOnPreferenceChangeListener((preference, newValue) -> {
            int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
            preference.setSummary(((ListPreference) preference).getEntries()[index]);
            return true;
        });
    }
}
