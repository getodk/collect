package org.odk.collect.android.application.initialization.migration;

import org.odk.collect.android.preferences.PreferencesDataSource;

public interface Migration {
    void apply(PreferencesDataSource prefs);
}
