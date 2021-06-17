package org.odk.collect.android.application.initialization;

import org.odk.collect.shared.Settings;

public interface SettingsMigrator {

    void migrate(Settings generalSettings, Settings adminSettings);
}
