package org.odk.collect.android.preferences.utilities;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;

public final class PreferencesUtils {

    private PreferencesUtils() {

    }

    public static void displayDisabled(CheckBoxPreference preference, boolean displayValue) {
        preference.setPersistent(false);
        preference.setEnabled(false);
        preference.setChecked(displayValue);
        preference.setPersistent(true);
    }

    public static void displayDisabled(Preference preference, String displayValue) {
        preference.setEnabled(false);
        preference.setSummaryProvider(pref -> displayValue);
    }
}
