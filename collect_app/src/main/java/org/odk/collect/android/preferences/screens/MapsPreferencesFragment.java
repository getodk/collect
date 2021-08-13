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

package org.odk.collect.android.preferences.screens;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import org.odk.collect.android.R;
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.geo.MapProvider;
import org.odk.collect.android.preferences.CaptionedListPreference;
import org.odk.collect.android.preferences.CaptionedListPreference.Item;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.android.preferences.dialogs.ReferenceLayerPreferenceDialog;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.shared.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.odk.collect.android.preferences.keys.ProjectKeys.CATEGORY_BASEMAP;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_BASEMAP_SOURCE;

public class MapsPreferencesFragment extends BaseProjectPreferencesFragment {

    private Context context;
    private ListPreference basemapSourcePref;
    private CaptionedListPreference referenceLayerPref;
    private boolean autoShowReferenceLayerDialog;

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            DialogFragment dialogFragment = null;
            if (preference instanceof CaptionedListPreference) {
                dialogFragment = ReferenceLayerPreferenceDialog.newInstance(preference.getKey());
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getParentFragmentManager(), ReferenceLayerPreferenceDialog.class.getName());
            }
        }
    }

    /** Pops up the preference dialog that lets the user choose a reference layer. */
    public static void showReferenceLayerDialog(Activity activity) {
        // Unfortunately, the Preference class is designed so that it is impossible
        // to just open a preference dialog without building a PreferenceFragment
        // and attaching it to an activity.  So, we instantiate a MapsPreference
        // fragment that is configured to immediately open the dialog when it's
        // attached, then instantiate it and attach it.
        MapsPreferencesFragment prefs = new MapsPreferencesFragment();
        prefs.autoShowReferenceLayerDialog = true;  // makes dialog open immediately
        ((AppCompatActivity) activity).getSupportFragmentManager()
            .beginTransaction()
            .add(prefs, null)
            .commit();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.maps_preferences, rootKey);

        context = getPreferenceScreen().getContext();
        initBasemapSourcePref();
        initReferenceLayerPref();
        if (autoShowReferenceLayerDialog) {
            populateReferenceLayerPref();
            /** Opens the dialog programmatically, rather than by a click from the user. */
            onDisplayPreferenceDialog(getPreferenceManager().findPreference("reference_layer"));
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (referenceLayerPref != null) {
            populateReferenceLayerPref();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        referenceLayerPref = null;
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
        basemapSourcePref.setIconSpaceReserved(false);
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
        PreferenceCategory baseCategory = findPreference(CATEGORY_BASEMAP);
        baseCategory.removeAll();
        baseCategory.addPreference(basemapSourcePref);

        for (Preference pref : cftor.createPrefs(context)) {
            pref.setIconSpaceReserved(false);
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
        referenceLayerPref = findPreference("reference_layer");
        referenceLayerPref.setOnPreferenceClickListener(preference -> {
            populateReferenceLayerPref();
            return false;
        });

        String layersPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.LAYERS);

        if (referenceLayerPref.getValue() == null
                || new File(PathUtils.getAbsoluteFilePath(layersPath, referenceLayerPref.getValue())).exists()) {
            updateReferenceLayerSummary(referenceLayerPref.getValue());
        } else {
            referenceLayerPref.setValue(null);
            updateReferenceLayerSummary(null);
        }
        referenceLayerPref.setOnPreferenceChangeListener((preference, newValue) -> {
            updateReferenceLayerSummary(newValue);
            DialogFragment dialogFragment = (DialogFragment) getParentFragmentManager().findFragmentByTag(ReferenceLayerPreferenceDialog.class.getName());
            if (dialogFragment != null) {
                dialogFragment.dismiss();
            }
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
                String path = PathUtils.getAbsoluteFilePath(new StoragePathProvider().getOdkDirPath(StorageSubdirectory.LAYERS), value.toString());
                summary = cftor.getDisplayName(new File(path));
            }
            referenceLayerPref.setSummary(summary);
        }
    }


    /** Sets up the contents of the reference layer selection dialog. */
    private void populateReferenceLayerPref() {
        MapConfigurator cftor = MapProvider.getConfigurator();
        StoragePathProvider storagePathProvider = new StoragePathProvider();

        List<Item> items = new ArrayList<>();
        items.add(new Item(null, getString(R.string.none), ""));
        for (File file : getSupportedLayerFiles(cftor)) {
            String path = FileUtils.simplifyScopedStoragePath(file.getPath());
            String value = PathUtils.getRelativeFilePath(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS), file.getAbsolutePath());
            String name = cftor.getDisplayName(new File(file.getAbsolutePath()));
            items.add(new Item(value, name, path));
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

        String layerDir = FileUtils.simplifyScopedStoragePath(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS));
        referenceLayerPref.setDialogCaption(context.getString(
            items.size() > 1 ? R.string.layer_data_caption : R.string.layer_data_caption_none,
            layerDir, context.getString(MapProvider.getSourceLabelId())
        ));

        referenceLayerPref.updateContent();
    }

    /** Gets the list of layer data files supported by the current MapConfigurator. */
    private static List<File> getSupportedLayerFiles(MapConfigurator cftor) {
        List<File> files = new ArrayList<>();
        for (File file : FileUtils.walk(new File(new StoragePathProvider().getOdkDirPath(StorageSubdirectory.LAYERS)))) {
            if (cftor.supportsLayer(file)) {
                files.add(file);
            }
        }
        return files;
    }
}
