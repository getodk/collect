package org.odk.collect.android.backgroundwork

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.formmanagement.InstancesAppState
import org.odk.collect.android.gdrive.GoogleAccountsManager
import org.odk.collect.android.gdrive.GoogleApiProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.instancemanagement.InstanceAutoSender
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.permissions.PermissionsProvider
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class AutoSendTaskSpecTest {

    private val instanceAutoSender = mock<InstanceAutoSender>()

    private lateinit var projectId: String

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
        })

        RobolectricHelpers.mountExternalStorage()
        projectId = CollectHelpers.setupDemoProject()
        TestSettingsProvider.getUnprotectedSettings(projectId)
            .save(ProjectKeys.KEY_AUTOSEND, "wifi_and_cellular")
    }

    @Test
    fun `passes project id`() {
        val inputData = mapOf(AutoSendTaskSpec.DATA_PROJECT_ID to projectId)
        AutoSendTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData).get()
        verify(instanceAutoSender).autoSendInstances(projectId)
    }
}
