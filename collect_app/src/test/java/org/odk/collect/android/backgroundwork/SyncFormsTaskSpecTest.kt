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
import org.odk.collect.async.TaskSpec
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
    fun `when DATA_LAST_UNIQUE_EXECUTION equals true getTask calls synchronize with proper data`() {
        val inputData = HashMap<String, String>()
        inputData[TaskSpec.DATA_PROJECT_ID] = "projectId"
        inputData[TaskSpec.DATA_LAST_UNIQUE_EXECUTION] = "true"
        SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData).get()
        verify(formsUpdater).matchFormsWithServer("projectId", true)
    }

    @Test
    fun `when DATA_LAST_UNIQUE_EXECUTION equals false getTask calls synchronize with proper data`() {
        val inputData = HashMap<String, String>()
        inputData[TaskSpec.DATA_PROJECT_ID] = "projectId"
        inputData[TaskSpec.DATA_LAST_UNIQUE_EXECUTION] = "false"
        SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData).get()
        verify(formsUpdater).matchFormsWithServer("projectId", false)
    }

    @Test
    fun `getTask returns proper result value`() {
        val inputData = HashMap<String, String>()
        inputData[TaskSpec.DATA_PROJECT_ID] = "projectId"
        inputData[TaskSpec.DATA_LAST_UNIQUE_EXECUTION] = "true"

        whenever(formsUpdater.matchFormsWithServer("projectId", true)).thenReturn(true)
        var result = SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData).get()
        assertThat(result, `is`(true))

        whenever(formsUpdater.matchFormsWithServer("projectId")).thenReturn(false)
        result = SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData).get()
        assertThat(result, `is`(false))
    }

    @Test
    fun `numberOfRetries should be limited`() {
        assertThat(SyncFormsTaskSpec().numberOfRetries, `is`(3))
    }
}
