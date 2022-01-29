package org.odk.collect.android.configure;

import org.odk.collect.shared.Settings;

public interface SettingsMigrator {

    void migrate(Settings generalSettings, Settings adminSettings);
}
