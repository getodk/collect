package org.odk.collect.android.application

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.settings.keys.ProjectKeys

class CollectSettingsChangeHandlerTest {

    private val propertyManager = mock<PropertyManager>()
    private val formUpdateScheduler = mock<FormUpdateScheduler>()
    private val formsDataService = mock<FormsDataService>()
    private val handler = CollectSettingsChangeHandler(propertyManager, formUpdateScheduler, formsDataService)

    @Test
    fun `updates PropertyManager when a single setting is changed`() {
        handler.onSettingChanged("projectId", "anything", "blah")
        verify(propertyManager).reload()
    }

    @Test
    fun `when changed key is FORM_UPDATE_MODE schedules updates`() {
        handler.onSettingChanged("projectId", "anything", ProjectKeys.KEY_FORM_UPDATE_MODE)
        verify(formUpdateScheduler).scheduleUpdates("projectId")
    }

    @Test
    fun `when changed key is PERIODIC_FORM_UPDATES_CHECK schedules updates`() {
        handler.onSettingChanged(
            "projectId",
            "anything",
            ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK
        )
        verify(formUpdateScheduler).scheduleUpdates("projectId")
    }

    @Test
    fun `when changed key is PROTOCOL schedules updates`() {
        handler.onSettingChanged("projectId", "anything", ProjectKeys.KEY_PROTOCOL)
        verify(formUpdateScheduler).scheduleUpdates("projectId")
    }

    @Test
    fun `when changed key is PROTOCOL clears sync status`() {
        handler.onSettingChanged("projectId", "anything", ProjectKeys.KEY_PROTOCOL)
        verify(formsDataService).clear("projectId")
    }

    @Test
    fun `when changed key is SERVER_URL clears sync status`() {
        handler.onSettingChanged("projectId", "anything", ProjectKeys.KEY_SERVER_URL)
        verify(formsDataService).clear("projectId")
    }

    @Test
    fun `do not schedule updates if other single settings are changed`() {
        handler.onSettingChanged("projectId", "anything", "blah")
        verifyNoInteractions(formUpdateScheduler)
    }

    @Test
    fun `do not clear sync status if other single settings are changed`() {
        handler.onSettingChanged("projectId", "anything", "blah")
        verifyNoInteractions(formsDataService)
    }

    @Test
    fun `updates PropertyManager when multiple settings are changed`() {
        handler.onSettingsChanged("projectId")
        verify(propertyManager).reload()
    }

    @Test
    fun `schedule updates when multiple settings are changes`() {
        handler.onSettingsChanged("projectId")
        verify(formUpdateScheduler).scheduleUpdates("projectId")
    }
}
