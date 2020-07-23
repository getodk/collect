package org.odk.collect.android.application.initialization;

import android.content.SharedPreferences;

public interface SettingsPreferenceMigrator {

    void migrate(SharedPreferences generalSharedPreferences, SharedPreferences adminSharedPreferences);
}
