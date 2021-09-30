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
package org.odk.collect.android.preferences.screens

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import org.odk.collect.android.R
import org.odk.collect.android.geo.MapConfigurator
import org.odk.collect.android.geo.MapProvider
import org.odk.collect.android.geo.ReferenceLayerRepository
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.CaptionedListPreference
import org.odk.collect.android.preferences.PrefUtils
import org.odk.collect.android.preferences.dialogs.ReferenceLayerPreferenceDialog
import org.odk.collect.android.preferences.keys.ProjectKeys.CATEGORY_BASEMAP
import org.odk.collect.android.preferences.keys.ProjectKeys.KEY_BASEMAP_SOURCE
import org.odk.collect.android.preferences.screens.ReferenceLayerPreferenceUtils.populateReferenceLayerPref
import org.odk.collect.android.utilities.MultiClickGuard.allowClick
import java.io.File
import javax.inject.Inject

class MapsPreferencesFragment : BaseProjectPreferencesFragment() {

    private lateinit var basemapSourcePref: ListPreference

    private var referenceLayerPref: CaptionedListPreference? = null
    private var autoShowReferenceLayerDialog = false

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (allowClick(javaClass.name)) {
            var dialogFragment: DialogFragment? = null
            if (preference is CaptionedListPreference) {
                dialogFragment = ReferenceLayerPreferenceDialog.newInstance(preference.getKey())
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(
                    parentFragmentManager,
                    ReferenceLayerPreferenceDialog::class.java.name
                )
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.maps_preferences, rootKey)
        initBasemapSourcePref()
        initReferenceLayerPref()
        if (autoShowReferenceLayerDialog) {
            populateReferenceLayerPref(requireContext(), referenceLayerRepository, referenceLayerPref!!)
            /** Opens the dialog programmatically, rather than by a click from the user.  */
            onDisplayPreferenceDialog(
                preferenceManager.findPreference("reference_layer")!!
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (referenceLayerPref != null) {
            populateReferenceLayerPref(requireContext(), referenceLayerRepository, referenceLayerPref!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        referenceLayerPref = null
    }

    /**
     * Creates the Basemap Source preference widget (but doesn't add it to
     * the screen; onBasemapSourceChanged will do that part).
     */
    private fun initBasemapSourcePref() {
        basemapSourcePref = PrefUtils.createListPref(
            context, KEY_BASEMAP_SOURCE, getString(R.string.basemap_source),
            MapProvider.getLabelIds(), MapProvider.getIds()
        )
        basemapSourcePref.setIconSpaceReserved(false)
        onBasemapSourceChanged(MapProvider.getConfigurator())
        basemapSourcePref.setOnPreferenceChangeListener { _: Preference?, value: Any ->
            val cftor = MapProvider.getConfigurator(value.toString())
            if (!cftor.isAvailable(context)) {
                cftor.showUnavailableMessage(context)
                false
            } else {
                onBasemapSourceChanged(cftor)
                true
            }
        }
    }

    /** Updates the rest of the preference UI when the Basemap Source is changed.  */
    private fun onBasemapSourceChanged(cftor: MapConfigurator) {
        // Set up the preferences in the "Basemap" section.
        val baseCategory = findPreference<PreferenceCategory>(CATEGORY_BASEMAP)
        baseCategory!!.removeAll()
        baseCategory.addPreference(basemapSourcePref)
        for (pref in cftor.createPrefs(context)) {
            pref.isIconSpaceReserved = false
            baseCategory.addPreference(pref)
        }

        // Clear the reference layer if it isn't supported by the new basemap.
        if (referenceLayerPref != null) {
            val path = referenceLayerPref!!.value
            if (path != null && !cftor.supportsLayer(File(path))) {
                referenceLayerPref!!.value = null
                updateReferenceLayerSummary(null)
            }
        }
    }

    /** Sets up listeners for the Reference Layer preference widget.  */
    private fun initReferenceLayerPref() {
        referenceLayerPref = findPreference("reference_layer")
        referenceLayerPref!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference? ->
                populateReferenceLayerPref(requireContext(), referenceLayerRepository, referenceLayerPref!!)
                false
            }
        if (referenceLayerPref!!.value == null || referenceLayerRepository.get(
                referenceLayerPref!!.value
            ) != null
        ) {
            updateReferenceLayerSummary(referenceLayerPref!!.value)
        } else {
            referenceLayerPref!!.value = null
            updateReferenceLayerSummary(null)
        }
        referenceLayerPref!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
                updateReferenceLayerSummary(newValue)
                val dialogFragment = parentFragmentManager.findFragmentByTag(
                    ReferenceLayerPreferenceDialog::class.java.name
                ) as DialogFragment?
                dialogFragment?.dismiss()
                true
            }
    }

    /** Sets the summary text for the reference layer to show the selected file.  */
    private fun updateReferenceLayerSummary(value: Any?) {
        if (referenceLayerPref != null) {
            val summary: String = if (value == null) {
                getString(R.string.none)
            } else {
                val referenceLayer = referenceLayerRepository.get(value.toString())

                if (referenceLayer != null) {
                    val path = referenceLayer.file.absolutePath
                    val cftor = MapProvider.getConfigurator()
                    cftor.getDisplayName(File(path))
                } else {
                    getString(R.string.none)
                }
            }

            referenceLayerPref!!.summary = summary
        }
    }

    companion object {

        /** Pops up the preference dialog that lets the user choose a reference layer.  */
        @JvmStatic
        fun showReferenceLayerDialog(activity: AppCompatActivity) {
            // Unfortunately, the Preference class is designed so that it is impossible
            // to just open a preference dialog without building a PreferenceFragment
            // and attaching it to an activity.  So, we instantiate a MapsPreference
            // fragment that is configured to immediately open the dialog when it's
            // attached, then instantiate it and attach it.
            val prefs = MapsPreferencesFragment()
            prefs.autoShowReferenceLayerDialog = true // makes dialog open immediately
            activity.supportFragmentManager
                .beginTransaction()
                .add(prefs, null)
                .commit()
        }
    }
}
