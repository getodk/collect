package org.odk.collect.android.preferences.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import org.odk.collect.android.R
import org.odk.collect.android.utilities.MultiClickGuard

class AccessControlPreferencesFragment :
    BaseAdminPreferencesFragment(),
    Preference.OnPreferenceClickListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.access_control_preferences, rootKey)

        findPreference<Preference>(MAIN_MENU_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(USER_SETTINGS_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(FORM_ENTRY_KEY)!!.onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            when (preference.key) {
                MAIN_MENU_KEY -> displayPreferences(MainMenuAccessPreferencesFragment())
                USER_SETTINGS_KEY -> displayPreferences(UserSettingsAccessPreferencesFragment())
                FORM_ENTRY_KEY -> displayPreferences(FormEntryAccessPreferencesFragment())
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

    companion object {
        private const val MAIN_MENU_KEY = "main_menu"
        private const val USER_SETTINGS_KEY = "user_settings"
        private const val FORM_ENTRY_KEY = "form_entry"
    }
}
