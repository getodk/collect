package org.odk.collect.settings;

import org.odk.collect.shared.Settings;

public interface SettingsMigrator {

    void migrate(Settings generalSettings, Settings adminSettings);
}
