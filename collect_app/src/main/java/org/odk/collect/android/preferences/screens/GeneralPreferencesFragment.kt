/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.preferences.screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.dialogs.ChangeAdminPasswordDialog
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.utilities.MultiClickGuard
import org.odk.collect.android.version.VersionInformation
import javax.inject.Inject

class GeneralPreferencesFragment :
    BaseGeneralPreferencesFragment(),
    Preference.OnPreferenceClickListener {

    @Inject
    lateinit var versionInformation: VersionInformation

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.general_preferences, rootKey)

        findPreference<Preference>("protocol")!!.onPreferenceClickListener = this
        findPreference<Preference>("project_display")!!.onPreferenceClickListener = this
        findPreference<Preference>("user_interface")!!.onPreferenceClickListener = this
        findPreference<Preference>("maps")!!.onPreferenceClickListener = this
        findPreference<Preference>("form_management")!!.onPreferenceClickListener = this
        findPreference<Preference>("user_and_device_identity")!!.onPreferenceClickListener = this
        findPreference<Preference>("experimental")!!.onPreferenceClickListener = this
        findPreference<Preference>(AdminKeys.KEY_CHANGE_ADMIN_PASSWORD)!!.onPreferenceClickListener = this
        findPreference<Preference>("project_management")!!.onPreferenceClickListener = this
        findPreference<Preference>("access_control")!!.onPreferenceClickListener = this

        if (!isInAdminMode) {
            setPreferencesVisibility()
        }
        if (versionInformation.isRelease) {
            findPreference<Preference>("experimental")!!.isVisible = false
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            when (preference.key) {
                AdminKeys.KEY_CHANGE_ADMIN_PASSWORD -> DialogUtils.showIfNotShowing(
                    ChangeAdminPasswordDialog::class.java, requireActivity().supportFragmentManager
                )
                else -> displayPreferences(getPreferenceFragment(preference.key))
            }
            return true
        }
        return false
    }

    private fun displayPreferences(fragment: Fragment?) {
        if (fragment != null) {
            fragment.arguments = arguments
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.preferences_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getPreferenceFragment(preferenceKey: String): PreferenceFragmentCompat? {
        return when (preferenceKey) {
            "protocol" -> ServerPreferencesFragment()
            "project_display" -> ProjectDisplayPreferencesFragment()
            "user_interface" -> UserInterfacePreferencesFragment()
            "maps" -> MapsPreferencesFragment()
            "form_management" -> FormManagementPreferencesFragment()
            "user_and_device_identity" -> IdentityPreferencesFragment()
            "experimental" -> ExperimentalPreferencesFragment()
            "project_management" -> ProjectManagementPreferencesFragment()
            "access_control" -> AccessControlPreferencesFragment()
            else -> null
        }
    }

    private fun setPreferencesVisibility() {
        val preferenceScreen = preferenceScreen
        if (!hasAtleastOneSettingEnabled(AdminKeys.serverKeys)) {
            preferenceScreen.removePreference(findPreference("protocol"))
        }
        if (!hasAtleastOneSettingEnabled(AdminKeys.userInterfaceKeys)) {
            preferenceScreen.removePreference(findPreference("user_interface"))
        }
        val mapsScreenEnabled = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_MAPS)
        if (!mapsScreenEnabled) {
            preferenceScreen.removePreference(findPreference("maps"))
        }
        if (!hasAtleastOneSettingEnabled(AdminKeys.formManagementKeys)) {
            preferenceScreen.removePreference(findPreference("form_management"))
        }
        if (!hasAtleastOneSettingEnabled(AdminKeys.identityKeys)) {
            preferenceScreen.removePreference(findPreference("user_and_device_identity"))
        }
    }

    private fun hasAtleastOneSettingEnabled(keys: Collection<String>): Boolean {
        for (key in keys) {
            val value = settingsProvider.getAdminSettings().getBoolean(key)
            if (value) {
                return true
            }
        }
        return false
    }
}
