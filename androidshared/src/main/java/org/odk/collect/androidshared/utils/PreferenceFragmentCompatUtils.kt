package org.odk.collect.androidshared.utils

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

fun <T : Preference> PreferenceFragmentCompat.getPreference(key: String): T {
    return this.findPreference(key)!!
}
