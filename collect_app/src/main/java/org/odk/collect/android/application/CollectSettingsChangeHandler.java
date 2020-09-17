package org.odk.collect.android.application;

import org.odk.collect.android.backgroundwork.FormUpdateManager;
import org.odk.collect.android.configure.ServerRepository;
import org.odk.collect.android.configure.SettingsChangeHandler;
import org.odk.collect.android.logic.PropertyManager;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_SERVER_URL;

public class CollectSettingsChangeHandler implements SettingsChangeHandler {

    private final PropertyManager propertyManager;
    private final FormUpdateManager formUpdateManager;
    private final ServerRepository serverRepository;

    public CollectSettingsChangeHandler(PropertyManager propertyManager, FormUpdateManager formUpdateManager, ServerRepository serverRepository) {
        this.propertyManager = propertyManager;
        this.formUpdateManager = formUpdateManager;
        this.serverRepository = serverRepository;
    }

    @Override
    public void onSettingChanged(String changedKey, Object newValue) {
        propertyManager.reload();

        if (changedKey.equals(KEY_FORM_UPDATE_MODE) || changedKey.equals(KEY_PERIODIC_FORM_UPDATES_CHECK) || changedKey.equals(KEY_PROTOCOL)) {
            formUpdateManager.scheduleUpdates();
        }

        if (changedKey.equals(KEY_SERVER_URL)) {
            serverRepository.save((String) newValue);
        }
    }
}
