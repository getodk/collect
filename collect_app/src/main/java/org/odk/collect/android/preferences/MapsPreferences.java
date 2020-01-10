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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.geo.MapProvider;
import org.odk.collect.android.preferences.CaptionedListPreference.Item;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.odk.collect.android.preferences.GeneralKeys.CATEGORY_BASEMAP;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASEMAP_SOURCE;
import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class MapsPreferences extends BasePreferenceFragment {
    private Context context;
    private ListPreference basemapSourcePref;
    private CaptionedListPreference referenceLayerPref;
    private boolean autoShowReferenceLayerDialog;

    public static MapsPreferences newInstance(boolean adminMode) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(INTENT_KEY_ADMIN_MODE, adminMode);
        MapsPreferences prefs = new MapsPreferences();
        prefs.setArguments(bundle);
        return prefs;
    }

    /** Pops up the preference dialog that lets the user choose a reference layer. */
    public static void showReferenceLayerDialog(Activity activity) {
        // Unfortunately, the Preference class is designed so that it is impossible
        // to just open a preference dialog without building a PreferenceFragment
        // and attaching it to an activity.  So, we instantiate a MapsPreference
        // fragment that is configured to immediately open the dialog when it's
        // attached, then instantiate it and attach it.
        MapsPreferences prefs = newInstance(false);
        prefs.autoShowReferenceLayerDialog = true;  // makes dialog open immediately
        activity.getFragmentManager()
            .beginTransaction()
            .add(prefs, null)
            .commit();
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.maps_preferences);

        context = getPreferenceScreen().getContext();
        initBasemapSourcePref();
        initReferenceLayerPref();
        if (autoShowReferenceLayerDialog) {
            populateReferenceLayerPref();
            referenceLayerPref.showDialog();
        }
    }

    /**
     * Creates the Basemap Source preference widget (but doesn't add it to
     * the screen; onBasemapSourceChanged will do that part).
     */
    private void initBasemapSourcePref() {
        basemapSourcePref = PrefUtils.createListPref(
            context, KEY_BASEMAP_SOURCE, getString(R.string.basemap_source),
            MapProvider.getLabelIds(), MapProvider.getIds()
        );
        onBasemapSourceChanged(MapProvider.getConfigurator());
        basemapSourcePref.setOnPreferenceChangeListener((pref, value) -> {
            MapConfigurator cftor = MapProvider.getConfigurator(value.toString());
            if (!cftor.isAvailable(context)) {
                cftor.showUnavailableMessage(context);
                return false;
            } else {
                onBasemapSourceChanged(cftor);
                return true;
            }
        });
    }

    /** Updates the rest of the preference UI when the Basemap Source is changed. */
    private void onBasemapSourceChanged(MapConfigurator cftor) {
        // Set up the preferences in the "Basemap" section.
        PreferenceCategory baseCategory = (PreferenceCategory) findPreference(CATEGORY_BASEMAP);
        baseCategory.removeAll();
        baseCategory.addPreference(basemapSourcePref);

        for (Preference pref : cftor.createPrefs(context)) {
            baseCategory.addPreference(pref);
        }

        // Clear the reference layer if it isn't supported by the new basemap.
        if (referenceLayerPref != null) {
            String path = referenceLayerPref.getValue();
            if (path != null && !cftor.supportsLayer(new File(path))) {
                referenceLayerPref.setValue(null);
                updateReferenceLayerSummary(null);
            }
        }
    }

    /** Sets up listeners for the Reference Layer preference widget. */
    private void initReferenceLayerPref() {
        referenceLayerPref = (CaptionedListPreference) findPreference("reference_layer");
        referenceLayerPref.setOnPreferenceClickListener(preference -> {
            populateReferenceLayerPref();
            return false;
        });
        updateReferenceLayerSummary(referenceLayerPref.getValue());
        referenceLayerPref.setOnPreferenceChangeListener((preference, newValue) -> {
            updateReferenceLayerSummary(newValue);
            return true;
        });
    }

    /** Sets the summary text for the reference layer to show the selected file. */
    private void updateReferenceLayerSummary(Object value) {
        if (referenceLayerPref != null) {
            String summary;
            if (value == null) {
                summary = getString(R.string.none);
            } else {
                MapConfigurator cftor = MapProvider.getConfigurator();
                summary = cftor.getDisplayName(new File(value.toString()));
            }
            referenceLayerPref.setSummary(summary);
        }
    }

    /** Sets up the contents of the reference layer selection dialog. */
    private void populateReferenceLayerPref() {
        MapConfigurator cftor = MapProvider.getConfigurator();

        List<Item> items = new ArrayList<>();
        items.add(new Item(null, getString(R.string.none), ""));
        for (File file : getSupportedLayerFiles(cftor)) {
            String path = FileUtils.simplifyPath(file).toString();
            String name = cftor.getDisplayName(file);
            items.add(new Item(path, name, path));
        }

        // Sort by display name, then by path for files with identical names.
        Collections.sort(items, (a, b) -> {
            if ((a.value == null) != (b.value == null)) {  // one or the other is null
                return a.value == null ? -1 : 1;
            }
            if (!a.label.equalsIgnoreCase(b.label)) {
                return a.label.compareToIgnoreCase(b.label);
            }
            if (!a.label.equals(b.label)) {
                return a.label.compareTo(b.label);
            }
            if (a.value != null && b.value != null) {
                return FileUtils.comparePaths(a.value, b.value);
            }
            return 0;  // both a.value and b.value are null
        });

        referenceLayerPref.setItems(items);

        File layerDir = FileUtils.simplifyPath(new File(Collect.OFFLINE_LAYERS));
        referenceLayerPref.setDialogCaption(context.getString(
            items.size() > 1 ? R.string.layer_data_caption : R.string.layer_data_caption_none,
            layerDir, context.getString(MapProvider.getSourceLabelId())
        ));

        referenceLayerPref.updateContent();
    }

    /** Gets the list of layer data files supported by the current MapConfigurator. */
    private static List<File> getSupportedLayerFiles(MapConfigurator cftor) {
        List<File> files = new ArrayList<>();
        for (File file : FileUtils.walk(new File(Collect.OFFLINE_LAYERS))) {
            if (cftor.supportsLayer(file)) {
                files.add(file);
            }
        }
        return files;
    }
}
