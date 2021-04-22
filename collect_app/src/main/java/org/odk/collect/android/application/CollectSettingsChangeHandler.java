package org.odk.collect.android.application;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.backgroundwork.FormUpdateManager;
import org.odk.collect.android.configure.ServerRepository;
import org.odk.collect.android.configure.SettingsChangeHandler;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.shared.Settings;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.shared.Md5;

import java.io.ByteArrayInputStream;

import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_EXTERNAL_APP_RECORDING;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_SERVER_URL;

public class CollectSettingsChangeHandler implements SettingsChangeHandler {

    private final PropertyManager propertyManager;
    private final FormUpdateManager formUpdateManager;
    private final ServerRepository serverRepository;
    private final Analytics analytics;
    private final SettingsProvider settingsProvider;

    public CollectSettingsChangeHandler(PropertyManager propertyManager, FormUpdateManager formUpdateManager, ServerRepository serverRepository, Analytics analytics, SettingsProvider settingsProvider) {
        this.propertyManager = propertyManager;
        this.formUpdateManager = formUpdateManager;
        this.serverRepository = serverRepository;
        this.analytics = analytics;
        this.settingsProvider = settingsProvider;
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

        if (changedKey.equals(KEY_EXTERNAL_APP_RECORDING) && !((Boolean) newValue)) {
            Settings generalSettings = settingsProvider.getGeneralSettings();
            String currentServerUrl = generalSettings.getString(KEY_SERVER_URL);
            String serverHash = Md5.getMd5Hash(new ByteArrayInputStream(currentServerUrl.getBytes()));

            analytics.logServerEvent(AnalyticsEvents.INTERNAL_RECORDING_OPT_IN, serverHash);
        }
    }
}
