package org.odk.collect.android.backgroundwork

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.R
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.preferences.FormUpdateMode.MATCH_EXACTLY
import org.odk.collect.android.preferences.FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.keys.ProjectKeys.KEY_FORM_UPDATE_MODE
import org.odk.collect.android.preferences.keys.ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK
import org.odk.collect.android.preferences.keys.ProjectKeys.KEY_PROTOCOL
import org.odk.collect.async.Scheduler

@RunWith(AndroidJUnit4::class)
class FormUpdateAndInstanceSubmitSchedulerTest {

    private val application by lazy { ApplicationProvider.getApplicationContext<Application>() }
    private val settingsProvider by lazy { TestSettingsProvider.getSettingsProvider() }
    private val scheduler = mock<Scheduler>()

    @Test
    fun `scheduleUpdates passes project id when scheduling previously downloaded only`() {
        val generalSettings = settingsProvider.getGeneralSettings("myProject")

        generalSettings.save(KEY_PROTOCOL, ProjectKeys.PROTOCOL_SERVER)
        generalSettings.save(KEY_FORM_UPDATE_MODE, PREVIOUSLY_DOWNLOADED_ONLY.getValue(application))
        generalSettings.save(
            KEY_PERIODIC_FORM_UPDATES_CHECK,
            application.getString(R.string.every_one_hour_value)
        )

        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleUpdates("myProject")
        verify(scheduler).networkDeferred(
            eq("serverPollingJob:myProject"),
            any<AutoUpdateTaskSpec>(),
            eq(3600000),
            eq(mapOf(AutoUpdateTaskSpec.DATA_PROJECT_ID to "myProject"))
        )
    }

    @Test
    fun `cancelUpdates cancels auto update for project`() {
        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.cancelUpdates("myProject")
        verify(scheduler).cancelDeferred("serverPollingJob:myProject")
    }

    @Test
    fun `cancelUpdates cancels match exactly update for project`() {
        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.cancelUpdates("myProject")
        verify(scheduler).cancelDeferred("match_exactly:myProject")
    }

    @Test
    fun `scheduleUpdates passes project id when scheduling match exactly`() {
        val generalSettings = settingsProvider.getGeneralSettings("myProject")

        generalSettings.save(KEY_PROTOCOL, ProjectKeys.PROTOCOL_SERVER)
        generalSettings.save(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(application))
        generalSettings.save(
            KEY_PERIODIC_FORM_UPDATES_CHECK,
            application.getString(R.string.every_one_hour_value)
        )

        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleUpdates("myProject")
        verify(scheduler).networkDeferred(
            eq("match_exactly:myProject"),
            any<SyncFormsTaskSpec>(),
            eq(3600000),
            eq(mapOf(SyncFormsTaskSpec.DATA_PROJECT_ID to "myProject"))
        )
    }

    @Test
    fun `scheduleSubmit passes current project ID`() {
        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleSubmit("myProject")
        verify(scheduler).networkDeferred(
            eq("AutoSendWorker:myProject"),
            any<AutoSendTaskSpec>(),
            eq(mapOf(AutoSendTaskSpec.DATA_PROJECT_ID to "myProject"))
        )
    }

    @Test
    fun `cancelSubmit cancels auto send for current project`() {
        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.cancelSubmit("myProject")
        verify(scheduler).cancelDeferred("AutoSendWorker:myProject")
    }
}
