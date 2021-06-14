package org.odk.collect.android.application;

import org.junit.Test;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.backgroundwork.FormUpdateScheduler;
import org.odk.collect.android.configure.ServerRepository;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class CollectSettingsChangeHandlerTest {

    private final PropertyManager propertyManager = mock(PropertyManager.class);
    private final FormUpdateScheduler formUpdateScheduler = mock(FormUpdateScheduler.class);
    private final ServerRepository serverRepository = mock(ServerRepository.class);

    CollectSettingsChangeHandler handler = new CollectSettingsChangeHandler(propertyManager, formUpdateScheduler, serverRepository, mock(Analytics.class), mock(SettingsProvider.class));

    @Test
    public void updatesPropertyManager() {
        handler.onSettingChanged("projectId", "anything", "blah");
        verify(propertyManager).reload();
    }

    @Test
    public void doesNotDoAnythingElse() {
        handler.onSettingChanged("projectId", "anything", "blah");
        verifyNoInteractions(formUpdateScheduler);
        verifyNoInteractions(serverRepository);
    }

    @Test
    public void whenChangedKeyIsFormUpdateMode_schedulesUpdates() {
        handler.onSettingChanged("projectId", "anything", GeneralKeys.KEY_FORM_UPDATE_MODE);
        verify(formUpdateScheduler).scheduleUpdates("projectId");
    }

    @Test
    public void whenChangedKeyIsPeriodicUpdatesCheck_schedulesUpdates() {
        handler.onSettingChanged("projectId", "anything", GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK);
        verify(formUpdateScheduler).scheduleUpdates("projectId");
    }

    @Test
    public void whenChangedKeyIsProtocol_schedulesUpdates() {
        handler.onSettingChanged("projectId", "anything", GeneralKeys.KEY_PROTOCOL);
        verify(formUpdateScheduler).scheduleUpdates("projectId");
    }

    @Test
    public void whenChangedKeyIsServerURL_savesURLToServerRepository() {
        handler.onSettingChanged("projectId", "http://newUrl", GeneralKeys.KEY_SERVER_URL);
        verify(serverRepository).save("http://newUrl");
    }
}
