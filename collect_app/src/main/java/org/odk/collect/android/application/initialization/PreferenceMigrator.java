package org.odk.collect.android.application.initialization;

import android.content.SharedPreferences;

public interface PreferenceMigrator {

    void migrate(SharedPreferences generalSharedPreferences, SharedPreferences adminSharedPreferences);
}
