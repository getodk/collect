package org.odk.collect.android.application.initialization;

import org.odk.collect.android.preferences.source.Settings;

public interface SettingsPreferenceMigrator {

    void migrate(Settings generalSettings, Settings adminSettings);
}
