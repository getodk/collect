package org.odk.collect.android.application;

import org.odk.collect.android.backgroundwork.FormUpdateManager;
import org.odk.collect.android.configure.SettingsChangeHandler;
import org.odk.collect.android.logic.PropertyManager;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;

public class CollectSettingsChangeHandler implements SettingsChangeHandler {

    private final PropertyManager propertyManager;
    private final FormUpdateManager formUpdateManager;

    public CollectSettingsChangeHandler(PropertyManager propertyManager, FormUpdateManager formUpdateManager) {
        this.propertyManager = propertyManager;
        this.formUpdateManager = formUpdateManager;
    }

    @Override
    public void onSettingChanged(String changedKey) {
        propertyManager.reload();

        if (changedKey.equals(KEY_FORM_UPDATE_MODE) || changedKey.equals(KEY_PERIODIC_FORM_UPDATES_CHECK)) {
            formUpdateManager.scheduleUpdates();
        }
    }
}
