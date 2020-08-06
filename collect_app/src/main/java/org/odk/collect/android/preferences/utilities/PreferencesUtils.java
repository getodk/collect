package org.odk.collect.android.preferences.utilities;

import androidx.preference.CheckBoxPreference;

public class PreferencesUtils {

    private PreferencesUtils() {

    }

    public static void displayDisabled(CheckBoxPreference preference, boolean displayValue) {
        preference.setPersistent(false);
        preference.setEnabled(false);
        preference.setChecked(displayValue);
        preference.setPersistent(true);
    }
}
