package org.odk.collect.android.backgroundwork

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.formmanagement.FormSourceProvider
import org.odk.collect.android.formmanagement.InstancesAppState
import org.odk.collect.android.gdrive.GoogleAccountsManager
import org.odk.collect.android.gdrive.GoogleApiProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.instancemanagement.autosend.InstanceAutoSender
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.projects.ProjectDependencyProvider
import org.odk.collect.android.projects.ProjectDependencyProviderFactory
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.androidshared.network.NetworkStateProvider
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class AutoSendTaskSpecTest {

    private val instanceAutoSender = mock<InstanceAutoSender>()
    private val projectDependencyProvider = mock<ProjectDependencyProvider>()
    private val projectDependencyProviderFactory = mock<ProjectDependencyProviderFactory>()

    private lateinit var projectId: String

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesInstanceAutoSender(
                networkStateProvider: NetworkStateProvider?,
                settingsProvider: SettingsProvider?,
                context: Context?,
                notifier: Notifier?,
                analytics: Analytics?,
                googleAccountsManager: GoogleAccountsManager?,
                googleApiProvider: GoogleApiProvider?,
                permissionsProvider: PermissionsProvider?,
                instancesAppState: InstancesAppState?
            ): InstanceAutoSender {
                return instanceAutoSender
            }

            override fun providesProjectDependencyProviderFactory(
                settingsProvider: SettingsProvider?,
                formsRepositoryProvider: FormsRepositoryProvider?,
                instancesRepositoryProvider: InstancesRepositoryProvider?,
                storagePathProvider: StoragePathProvider?,
                changeLockProvider: ChangeLockProvider?,
                formSourceProvider: FormSourceProvider?
            ): ProjectDependencyProviderFactory {
                return projectDependencyProviderFactory
            }
        })

        RobolectricHelpers.mountExternalStorage()
        projectId = CollectHelpers.setupDemoProject()
        TestSettingsProvider.getUnprotectedSettings(projectId)
            .save(ProjectKeys.KEY_AUTOSEND, "wifi_and_cellular")

        whenever(projectDependencyProviderFactory.create(projectId)).thenReturn(projectDependencyProvider)
    }

    @Test
    fun `passes projectDependencyProvider with proper project id`() {
        val inputData = mapOf(TaskData.DATA_PROJECT_ID to projectId)
        AutoSendTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, true).get()
        verify(instanceAutoSender).autoSendInstances(projectDependencyProvider)
    }

    @Test
    fun `maxRetries should not be limited`() {
        assertThat(AutoSendTaskSpec().maxRetries, `is`(nullValue()))
    }
}
