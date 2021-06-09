package org.odk.collect.android.configure;

public interface SettingsChangeHandler {
    void onSettingChanged(String projectId, Object newValue, String changedKey);
}
