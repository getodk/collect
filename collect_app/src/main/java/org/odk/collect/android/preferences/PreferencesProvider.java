package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesProvider {
    private final Context context;

    public PreferencesProvider(Context context) {
        this.context = context;
    }

    public SharedPreferences getGeneralSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}

