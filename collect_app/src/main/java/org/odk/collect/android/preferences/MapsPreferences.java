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

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.view.View;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.map.BaseLayerSource;
import org.odk.collect.android.map.MapConfigurator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

import static org.odk.collect.android.preferences.GeneralKeys.CATEGORY_BASE_LAYER;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASE_LAYER_SOURCE;
import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class MapsPreferences extends BasePreferenceFragment {
    private Context context;
    private ListPreference baseLayerSourcePref;
    private CaptionedListPreference referenceLayerPref;

    public static MapsPreferences newInstance(boolean adminMode) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(INTENT_KEY_ADMIN_MODE, adminMode);

        MapsPreferences prefs = new MapsPreferences();
        prefs.setArguments(bundle);
        return prefs;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.maps_preferences);

        context = getPreferenceScreen().getContext();
        initBaseLayerSourcePref();
        initReferenceLayerPref();
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setTitle(R.string.maps);
    }

    @Override public void onDetach() {
        super.onDetach();
        if (toolbar != null) {
            toolbar.setTitle(R.string.general_preferences);
        }
    }

    /**
     * Creates the Base Layer Source preference widget (but doesn't add it to
     * the screen; onBaseLayerSourceChanged will do that part).
     */
    private void initBaseLayerSourcePref() {
        baseLayerSourcePref = PrefUtils.createListPref(
            context, KEY_BASE_LAYER_SOURCE, R.string.base_layer_source,
            MapConfigurator.getLabelIds(), MapConfigurator.getIds()
        );
        onBaseLayerSourceChanged(null);
        baseLayerSourcePref.setOnPreferenceChangeListener((pref, value) -> {
            onBaseLayerSourceChanged(value.toString());
            return true;
        });
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

    private void updateReferenceLayerSummary(Object value) {
        if (referenceLayerPref != null) {
            referenceLayerPref.setSummary(
                value != null ? value.toString() : getString(R.string.none));
        }
    }

    /** Updates the rest of the preference UI when the Base Layer Source is changed. */
    private void onBaseLayerSourceChanged(String id) {
        MapConfigurator.Option option = id != null ? MapConfigurator.get(id)
            : MapConfigurator.getCurrent(context);
        if (option != null) {
            // Set up the prefences in the "Base Layer" section.
            PreferenceCategory baseCategory = (PreferenceCategory) findPreference(CATEGORY_BASE_LAYER);
            baseCategory.removeAll();
            baseCategory.addPreference(baseLayerSourcePref);
            if (!option.source.isAvailable(context)) {
                option.source.showUnavailableMessage(context);
                return;
            }
            option.source.addPrefs(baseCategory);

            // Clear the reference layer if it isn't supported by the new base layer.
            if (referenceLayerPref != null) {
                String path = referenceLayerPref.getValue();
                if (path != null && !option.source.supportsLayer(new File(path))) {
                    referenceLayerPref.setValue(null);
                    updateReferenceLayerSummary(null);
                }
            }
        }
    }

    /** Sets up the contents of the reference layer selection dialog. */
    private void populateReferenceLayerPref() {
        MapConfigurator.Option option = MapConfigurator.getCurrent(context);

        List<File> files = getSupportedLayerFiles(option.source);
        String[] values = new String[files.size() + 1];
        String[] labels = new String[files.size() + 1];
        String[] captions = new String[files.size() + 1];
        values[0] = null;
        labels[0] = getString(R.string.none);
        captions[0] = "";
        for (int i = 0; i < files.size(); i++) {
            values[i + 1] = files.get(i).getAbsolutePath();
            labels[i + 1] = option.source.getDisplayName(files.get(i));
            captions[i + 1] = files.get(i).getAbsolutePath();
        }
        referenceLayerPref.setItems(values, labels, captions);

        referenceLayerPref.setDialogCaption(context.getString(
            files.isEmpty() ? R.string.layer_data_caption_none : R.string.layer_data_caption,
            Collect.OFFLINE_LAYERS, context.getString(option.sourceLabelId)
        ));

        referenceLayerPref.updateContent();
    }

    /** Gets the list of layer data files supported by the current BaseLayerSource. */
    private static List<File> getSupportedLayerFiles(BaseLayerSource source) {
        List<File> files = new ArrayList<>();
        for (File file : new File(Collect.OFFLINE_LAYERS).listFiles()) {
            if (source.supportsLayer(file)) {
                files.add(file);
            }
        }
        return files;
    }
}
