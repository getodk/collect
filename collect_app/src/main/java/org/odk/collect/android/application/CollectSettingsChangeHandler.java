package org.odk.collect.android.application;

import static org.odk.collect.android.analytics.AnalyticsUtils.logServerConfiguration;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_PROTOCOL;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_SERVER_URL;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.backgroundwork.FormUpdateScheduler;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.settings.importing.SettingsChangeHandler;

public class CollectSettingsChangeHandler implements SettingsChangeHandler {

    private final PropertyManager propertyManager;
    private final FormUpdateScheduler formUpdateScheduler;
    private final Analytics analytics;

    public CollectSettingsChangeHandler(PropertyManager propertyManager, FormUpdateScheduler formUpdateScheduler, Analytics analytics) {
        this.propertyManager = propertyManager;
        this.formUpdateScheduler = formUpdateScheduler;
        this.analytics = analytics;
    }

    @Override
    public void onSettingChanged(String projectId, Object newValue, String changedKey) {
        propertyManager.reload();

        if (changedKey.equals(KEY_FORM_UPDATE_MODE) || changedKey.equals(KEY_PERIODIC_FORM_UPDATES_CHECK) || changedKey.equals(KEY_PROTOCOL)) {
            formUpdateScheduler.scheduleUpdates(projectId);
        }

        if (changedKey.equals(KEY_SERVER_URL)) {
            logServerConfiguration(analytics, newValue.toString());
        }
    }
}
