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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.preference.Preference
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.ProjectPreferencesViewModel
import org.odk.collect.android.preferences.dialogs.AdminPasswordDialogFragment
import org.odk.collect.android.preferences.dialogs.ChangeAdminPasswordDialog
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

class ProjectPreferencesFragment(private val inFormEntry: Boolean) :
    BaseProjectPreferencesFragment(),
    Preference.OnPreferenceClickListener {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        projectPreferencesViewModel.state.observe(
            this,
            { state: Consumable<ProjectPreferencesViewModel.State> ->
                if (!state.isConsumed()) {
                    state.consume()
                    preferenceVisibilityHandler.updatePreferencesVisibility(preferenceScreen, state.value)
                    requireActivity().invalidateOptionsMenu()
                }
            }
        )
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.project_preferences, rootKey)

        findPreference<Preference>(PROTOCOL_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(PROJECT_DISPLAY_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(USER_INTERFACE_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(MAPS_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(FORM_MANAGEMENT_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(USER_AND_DEVICE_IDENTITY_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(EXPERIMENTAL_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(UNLOCK_PROTECTED_SETTINGS_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(CHANGE_ADMIN_PASSWORD_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(ACCESS_CONTROL_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(PROJECT_MANAGEMENT_PREFERENCE_KEY)!!.also {
            it.onPreferenceClickListener = this
            if (inFormEntry) {
                it.isEnabled = false
                it.setSummary(org.odk.collect.strings.R.string.setting_not_available_during_form_entry)
            }
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            when (preference.key) {
                PROTOCOL_PREFERENCE_KEY -> displayPreferences(ServerPreferencesFragment())
                PROJECT_DISPLAY_PREFERENCE_KEY -> displayPreferences(ProjectDisplayPreferencesFragment())
                USER_INTERFACE_PREFERENCE_KEY -> {
                    val fragment = UserInterfacePreferencesFragment()
                    fragment.arguments =
                        bundleOf(UserInterfacePreferencesFragment.ARG_IN_FORM_ENTRY to inFormEntry)
                    displayPreferences(fragment)
                }
                MAPS_PREFERENCE_KEY -> displayPreferences(MapsPreferencesFragment())
                FORM_MANAGEMENT_PREFERENCE_KEY -> displayPreferences(FormManagementPreferencesFragment())
                USER_AND_DEVICE_IDENTITY_PREFERENCE_KEY -> displayPreferences(IdentityPreferencesFragment())
                EXPERIMENTAL_PREFERENCE_KEY -> displayPreferences(ExperimentalPreferencesFragment())
                UNLOCK_PROTECTED_SETTINGS_PREFERENCE_KEY -> DialogFragmentUtils.showIfNotShowing(
                    AdminPasswordDialogFragment::class.java,
                    requireActivity().supportFragmentManager
                )
                CHANGE_ADMIN_PASSWORD_PREFERENCE_KEY -> DialogFragmentUtils.showIfNotShowing(
                    ChangeAdminPasswordDialog::class.java,
                    requireActivity().supportFragmentManager
                )
                PROJECT_MANAGEMENT_PREFERENCE_KEY -> displayPreferences(ProjectManagementPreferencesFragment())
                ACCESS_CONTROL_PREFERENCE_KEY -> displayPreferences(AccessControlPreferencesFragment())
            }
            return true
        }
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        when {
            projectPreferencesViewModel.isStateLocked() -> {
                menu.findItem(R.id.menu_locked).isVisible = true
                menu.findItem(R.id.menu_unlocked).isVisible = false
            }
            projectPreferencesViewModel.isStateUnlocked() -> {
                menu.findItem(R.id.menu_locked).isVisible = false
                menu.findItem(R.id.menu_unlocked).isVisible = true
            }
            else -> {
                menu.findItem(R.id.menu_locked).isVisible = false
                menu.findItem(R.id.menu_unlocked).isVisible = false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true) // Needs to be here rather than `onAttach` or crash can occur when switching system theme
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.project_preferences_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_locked) {
            DialogFragmentUtils.showIfNotShowing(
                AdminPasswordDialogFragment::class.java,
                requireActivity().supportFragmentManager
            )
            return true
        }
        return false
    }

    companion object {
        private const val PROTOCOL_PREFERENCE_KEY = "protocol"
        private const val PROJECT_DISPLAY_PREFERENCE_KEY = "project_display"
        private const val USER_INTERFACE_PREFERENCE_KEY = "user_interface"
        private const val MAPS_PREFERENCE_KEY = "maps"
        private const val FORM_MANAGEMENT_PREFERENCE_KEY = "form_management"
        private const val USER_AND_DEVICE_IDENTITY_PREFERENCE_KEY = "user_and_device_identity"
        private const val EXPERIMENTAL_PREFERENCE_KEY = "experimental"
        private const val UNLOCK_PROTECTED_SETTINGS_PREFERENCE_KEY = "unlock_protected_settings"
        private const val CHANGE_ADMIN_PASSWORD_PREFERENCE_KEY = "admin_password"
        private const val PROJECT_MANAGEMENT_PREFERENCE_KEY = "project_management"
        private const val ACCESS_CONTROL_PREFERENCE_KEY = "access_control"
    }
}
