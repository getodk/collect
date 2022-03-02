package org.odk.collect.android.backgroundwork

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.formmanagement.FormSourceProvider
import org.odk.collect.android.formmanagement.FormsUpdater
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.settings.SettingsProvider

@RunWith(AndroidJUnit4::class)
class SyncFormsTaskSpecTest {
    private val formsUpdater = mock<FormsUpdater>()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesFormUpdateChecker(
                context: Context,
                notifier: Notifier,
                analytics: Analytics,
                storagePathProvider: StoragePathProvider,
                settingsProvider: SettingsProvider,
                formsRepositoryProvider: FormsRepositoryProvider,
                formSourceProvider: FormSourceProvider,
                syncStatusAppState: SyncStatusAppState,
                instancesRepositoryProvider: InstancesRepositoryProvider,
                changeLockProvider: ChangeLockProvider
            ): FormsUpdater {
                return formsUpdater
            }
        })
    }

    @Test
    fun `when isLastUniqueExecution equals true task calls synchronize with notify true`() {
        val inputData = HashMap<String, String>()
        inputData[TaskData.DATA_PROJECT_ID] = "projectId"
        SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, true).get()
        verify(formsUpdater).matchFormsWithServer("projectId", true)
    }

    @Test
    fun `when isLastUniqueExecution equals false task calls synchronize with notify false`() {
        val inputData = HashMap<String, String>()
        inputData[TaskData.DATA_PROJECT_ID] = "projectId"
        SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, false).get()
        verify(formsUpdater).matchFormsWithServer("projectId", false)
    }

    @Test
    fun `task returns result from FormUpdater`() {
        val inputData = HashMap<String, String>()
        inputData[TaskData.DATA_PROJECT_ID] = "projectId"

        whenever(formsUpdater.matchFormsWithServer("projectId", true)).thenReturn(true)
        var result = SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, true).get()
        assertThat(result, `is`(true))

        whenever(formsUpdater.matchFormsWithServer("projectId")).thenReturn(false)
        result = SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, false).get()
        assertThat(result, `is`(false))
    }

    @Test
    fun `maxRetries should be limited`() {
        assertThat(SyncFormsTaskSpec().maxRetries, `is`(3))
    }
}
