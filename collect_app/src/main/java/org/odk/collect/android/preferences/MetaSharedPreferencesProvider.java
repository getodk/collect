package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class MetaSharedPreferencesProvider {

    private static final String PREFS_NAME = "meta";

    private final Context context;

    public MetaSharedPreferencesProvider(Context context) {
        this.context = context;
    }

    public SharedPreferences getMetaSharedPreferences() {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

