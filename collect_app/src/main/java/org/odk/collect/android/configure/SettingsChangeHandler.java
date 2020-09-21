package org.odk.collect.android.configure;

public interface SettingsChangeHandler {
    void onSettingChanged(String changedKey, Object newValue);
}
