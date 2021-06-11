package org.odk.collect.android.backgroundwork

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
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
import org.odk.collect.android.preferences.Protocol.ODK
import org.odk.collect.android.preferences.keys.GeneralKeys.KEY_FORM_UPDATE_MODE
import org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK
import org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PROTOCOL
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.async.Scheduler

@RunWith(AndroidJUnit4::class)
class FormUpdateAndInstanceSubmitSchedulerTest {

    private val application by lazy { ApplicationProvider.getApplicationContext<Application>() }
    private val settingsProvider by lazy { TestSettingsProvider.getSettingsProvider() }
    private val scheduler = mock<Scheduler>()

    private lateinit var projectId: String

    @Before
    fun setup() {
        projectId = CollectHelpers.setupDemoProject()
    }

    @Test
    fun `scheduleUpdates passes current project id when scheduling previously downloaded only`() {
        val generalSettings = settingsProvider.getGeneralSettings()

        generalSettings.save(KEY_PROTOCOL, ODK.getValue(application))
        generalSettings.save(KEY_FORM_UPDATE_MODE, PREVIOUSLY_DOWNLOADED_ONLY.getValue(application))
        generalSettings.save(
            KEY_PERIODIC_FORM_UPDATES_CHECK,
            application.getString(R.string.every_one_hour_value)
        )

        val manager =
            FormUpdateAndInstanceSubmitScheduler(
                scheduler,
                settingsProvider,
                application
            )

        manager.scheduleUpdates()
        verify(scheduler).networkDeferred(
            eq("serverPollingJob:$projectId"),
            any<AutoUpdateTaskSpec>(),
            eq(3600000),
            eq(mapOf(AutoUpdateTaskSpec.DATA_PROJECT_ID to projectId))
        )
    }

    @Test
    fun `cancelUpdates cancels auto update for current project`() {
        val manager =
            FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.cancelUpdates()
        verify(scheduler).cancelDeferred("serverPollingJob:$projectId")
    }

    @Test
    fun `cancelUpdates cancels match exactly update for current project`() {
        val manager =
            FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.cancelUpdates()
        verify(scheduler).cancelDeferred("match_exactly:$projectId")
    }

    @Test
    fun `scheduleUpdates passes current project id when scheduling match exactly`() {
        val generalSettings = settingsProvider.getGeneralSettings()

        generalSettings.save(KEY_PROTOCOL, ODK.getValue(application))
        generalSettings.save(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(application))
        generalSettings.save(
            KEY_PERIODIC_FORM_UPDATES_CHECK,
            application.getString(R.string.every_one_hour_value)
        )

        val manager =
            FormUpdateAndInstanceSubmitScheduler(
                scheduler,
                settingsProvider,
                application
            )

        manager.scheduleUpdates()
        verify(scheduler).networkDeferred(
            eq("match_exactly:$projectId"),
            any<SyncFormsTaskSpec>(),
            eq(3600000),
            eq(mapOf(SyncFormsTaskSpec.DATA_PROJECT_ID to projectId))
        )
    }
}
