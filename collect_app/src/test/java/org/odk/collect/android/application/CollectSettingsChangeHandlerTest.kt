package org.odk.collect.android.application

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.preferences.Defaults
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.settings.keys.ProjectKeys

@RunWith(AndroidJUnit4::class)
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
    fun `when changed key is SERVER_URL clears forms data`() {
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
        handler.onSettingsChanged("projectId", emptyList(), emptyList())
        verify(propertyManager).reload()
    }

    @Test
    fun `schedules updates when settings change and include form update-related ones`() {
        val relevantKeys = setOf(ProjectKeys.KEY_FORM_UPDATE_MODE, ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)
        val allUnprotectedKeys = Defaults.unprotected.keys
        val allProtectedKeys = Defaults.protected.keys

        allUnprotectedKeys.forEach { key ->
            handler.onSettingsChanged("projectId", listOf(key), emptyList())

            if (key in relevantKeys) {
                verify(formUpdateScheduler).scheduleUpdates("projectId")
            } else {
                verify(formUpdateScheduler, never()).scheduleUpdates("projectId")
            }

            clearInvocations(formUpdateScheduler)
        }

        allProtectedKeys.forEach { key ->
            handler.onSettingsChanged("projectId", emptyList(), listOf(key))
            verify(formUpdateScheduler, never()).scheduleUpdates("projectId")
        }
    }
}
