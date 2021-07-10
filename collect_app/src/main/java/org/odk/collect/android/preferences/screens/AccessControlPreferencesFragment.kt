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

        findPreference<Preference>("main_menu")!!.onPreferenceClickListener = this
        findPreference<Preference>("user_settings")!!.onPreferenceClickListener = this
        findPreference<Preference>("form_entry")!!.onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            when (preference.key) {
                "main_menu" -> displayPreferences(MainMenuAccessPreferencesFragment())
                "user_settings" -> displayPreferences(UserSettingsAccessPreferencesFragment())
                "form_entry" -> displayPreferences(FormEntryAccessPreferencesFragment())
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
}
