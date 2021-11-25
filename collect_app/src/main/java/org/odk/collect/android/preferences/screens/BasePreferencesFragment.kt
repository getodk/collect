package org.odk.collect.android.preferences.screens

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.DialogPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.odk.collect.android.R
import org.odk.collect.android.configure.SettingsChangeHandler
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.androidshared.utils.AppBarUtils
import org.odk.collect.shared.Settings.OnSettingChangeListener
import javax.inject.Inject

abstract class BasePreferencesFragment : PreferenceFragmentCompat(), OnSettingChangeListener {
    @Inject
    lateinit var settingsChangeHandler: SettingsChangeHandler

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        super.onDisplayPreferenceDialog(preference)

        // If we don't do this there is extra padding on "Cancel" and "OK" on
        // the preference dialogs. This appears to have something to with the `updateLocale`
        // calls in `CollectAbstractActivity` and weirdly only happens for English.
        val dialogPreference = preference as DialogPreference
        dialogPreference.setNegativeButtonText(R.string.cancel)
        dialogPreference.setPositiveButtonText(R.string.ok)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().let {
            AppBarUtils.setupAppBarLayout(it, preferenceScreen.title)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    protected fun displayPreferences(fragment: Fragment?) {
        if (fragment != null) {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.preferences_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
