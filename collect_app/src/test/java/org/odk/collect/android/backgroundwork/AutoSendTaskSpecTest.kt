package org.odk.collect.android.backgroundwork

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.backgroundwork.autosend.FormLevelAutoSendChecker
import org.odk.collect.android.backgroundwork.autosend.GeneralAutoSendChecker
import org.odk.collect.android.formmanagement.InstancesAppState
import org.odk.collect.android.gdrive.GoogleAccountsManager
import org.odk.collect.android.gdrive.GoogleApiProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.instancemanagement.InstanceAutoSender
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class AutoSendTaskSpecTest {

    private val instanceAutoSender = mock<InstanceAutoSender>()

    private lateinit var projectId: String
    private val generalAutoSendChecker: GeneralAutoSendChecker = mock()
    private val formLevelAutoSendChecker: FormLevelAutoSendChecker = mock()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesInstanceAutoSender(
                context: Context,
                changeLockProvider: ChangeLockProvider?,
                notifier: Notifier,
                analytics: Analytics,
                formsRepositoryProvider: FormsRepositoryProvider,
                instancesRepositoryProvider: InstancesRepositoryProvider,
                googleAccountsManager: GoogleAccountsManager,
                googleApiProvider: GoogleApiProvider,
                permissionsProvider: PermissionsProvider,
                settingsProvider: SettingsProvider,
                instancesAppState: InstancesAppState
            ): InstanceAutoSender {
                return instanceAutoSender
            }

            override fun providesGeneralAutoSendChecker(
                networkStateProvider: NetworkStateProvider,
                settingsProvider: SettingsProvider
            ): GeneralAutoSendChecker {
                return generalAutoSendChecker
            }

            override fun providesFormLevelAutoSendChecker(formsRepositoryProvider: FormsRepositoryProvider): FormLevelAutoSendChecker {
                return formLevelAutoSendChecker
            }
        })

        RobolectricHelpers.mountExternalStorage()
        projectId = CollectHelpers.setupDemoProject()
        TestSettingsProvider.getUnprotectedSettings(projectId)
            .save(ProjectKeys.KEY_AUTOSEND, "wifi_and_cellular")
    }

    @Test
    fun `maxRetries should not be limited`() {
        assertThat(AutoSendTaskSpec().maxRetries, `is`(nullValue()))
    }

    @Test
    fun `if autosend is disabled in settings and there are no forms with autosend enabled on a form level, autosender should not be triggered`() {
        whenever(generalAutoSendChecker.isAutoSendEnabled(projectId)).thenReturn(false)
        whenever(formLevelAutoSendChecker.isAutoSendEnabled(projectId)).thenReturn(false)

        triggerAutoSendTask()

        verifyNoInteractions(instanceAutoSender)
    }

    @Test
    fun `if autosend is enabled in settings and there are no forms with autosend enabled on a form level, autosender should be triggered`() {
        whenever(generalAutoSendChecker.isAutoSendEnabled(projectId)).thenReturn(true)
        whenever(formLevelAutoSendChecker.isAutoSendEnabled(projectId)).thenReturn(false)

        triggerAutoSendTask()

        verify(instanceAutoSender).autoSendInstances(projectId)
    }

    @Test
    fun `if autosend is disabled in settings but there are forms with autosend enabled on a form level, autosender should be triggered`() {
        whenever(generalAutoSendChecker.isAutoSendEnabled(projectId)).thenReturn(false)
        whenever(formLevelAutoSendChecker.isAutoSendEnabled(projectId)).thenReturn(true)

        triggerAutoSendTask()

        verify(instanceAutoSender).autoSendInstances(projectId)
    }

    @Test
    fun `if autosend is enabled in settings and there are forms with autosend enabled on a form level, autosender should be triggered`() {
        whenever(generalAutoSendChecker.isAutoSendEnabled(projectId)).thenReturn(true)
        whenever(formLevelAutoSendChecker.isAutoSendEnabled(projectId)).thenReturn(true)

        triggerAutoSendTask()

        verify(instanceAutoSender).autoSendInstances(projectId)
    }

    @Test
    fun `if autosend is disabled in settings and there are no forms with autosend enabled on a form level, task should return false`() {
        whenever(generalAutoSendChecker.isAutoSendEnabled(projectId)).thenReturn(false)
        whenever(formLevelAutoSendChecker.isAutoSendEnabled(projectId)).thenReturn(false)

        assertFalse(triggerAutoSendTask())
    }

    @Test
    fun `if autosender is triggered, task should return result from autosender`() {
        whenever(generalAutoSendChecker.isAutoSendEnabled(projectId)).thenReturn(true)
        whenever(instanceAutoSender.autoSendInstances(projectId)).thenReturn(true)

        assertTrue(triggerAutoSendTask())

        whenever(instanceAutoSender.autoSendInstances(projectId)).thenReturn(false)

        assertFalse(triggerAutoSendTask())
    }

    private fun triggerAutoSendTask(): Boolean {
        val inputData = mapOf(TaskData.DATA_PROJECT_ID to projectId)
        return AutoSendTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, true).get()
    }
}
