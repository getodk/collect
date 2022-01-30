package org.odk.collect.settings;

public interface SettingsChangeHandler {
    void onSettingChanged(String projectId, Object newValue, String changedKey);
}
