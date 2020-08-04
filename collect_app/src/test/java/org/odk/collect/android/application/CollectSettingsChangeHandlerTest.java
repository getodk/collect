package org.odk.collect.android.application;

import org.junit.Test;
import org.odk.collect.android.backgroundwork.FormUpdateManager;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.GeneralKeys;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CollectSettingsChangeHandlerTest {

    private final PropertyManager propertyManager = mock(PropertyManager.class);
    private final FormUpdateManager formUpdateManager = mock(FormUpdateManager.class);

    CollectSettingsChangeHandler handler = new CollectSettingsChangeHandler(propertyManager, formUpdateManager);

    @Test
    public void updatesPropertyManager() {
        handler.onSettingChanged("blah");
        verify(propertyManager).reload();
    }

    @Test
    public void doesNotScheduleUpdates() {
        handler.onSettingChanged("blah");
        verify(formUpdateManager, never()).scheduleUpdates();
    }

    @Test
    public void whenChangedKeyIsFormUpdateMode_schedulesUpdates() {
        handler.onSettingChanged(GeneralKeys.KEY_FORM_UPDATE_MODE);
        verify(formUpdateManager).scheduleUpdates();
    }

    @Test
    public void whenChangedKeyIsPeriodicUpdatesCheck_schedulesUpdates() {
        handler.onSettingChanged(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK);
        verify(formUpdateManager).scheduleUpdates();
    }
}