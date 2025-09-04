package org.odk.collect.android.backgroundwork

import android.app.Application
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
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.injection.config.ProjectDependencyModuleFactory
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider

@RunWith(AndroidJUnit4::class)
class SyncFormsTaskSpecTest {
    private val formsDataService = mock<FormsDataService>()
    private val notifier = mock<Notifier>()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesFormsUpdater(
                application: Application,
                notifier: Notifier,
                projectDependencyModuleFactory: ProjectDependencyModuleFactory
            ): FormsDataService {
                return formsDataService
            }

            override fun providesNotifier(
                application: Application,
                settingsProvider: SettingsProvider,
                projectsRepository: ProjectsRepository
            ): Notifier {
                return notifier
            }
        })
    }

    @Test
    fun `#getTask calls synchronize with notify true when isLastUniqueExecution equals true`() {
        val inputData = HashMap<String, String>().also {
            it[TaskData.DATA_PROJECT_ID] = "projectId"
        }
        SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, true).get()
        verify(formsDataService).matchFormsWithServer("projectId", true)
    }

    @Test
    fun `#getTask calls synchronize with notify false when isLastUniqueExecution equals false`() {
        val inputData = HashMap<String, String>().also {
            it[TaskData.DATA_PROJECT_ID] = "projectId"
        }
        SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, false).get()
        verify(formsDataService).matchFormsWithServer("projectId", false)
    }

    @Test
    fun `#getTask returns result from FormUpdater`() {
        val inputData = HashMap<String, String>().also {
            it[TaskData.DATA_PROJECT_ID] = "projectId"
        }
        whenever(formsDataService.matchFormsWithServer("projectId", true)).thenReturn(true)
        var result = SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, true).get()
        assertThat(result, `is`(true))

        whenever(formsDataService.matchFormsWithServer("projectId")).thenReturn(false)
        result = SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, false).get()
        assertThat(result, `is`(false))
    }

    @Test
    fun `maxRetries should be limited`() {
        assertThat(SyncFormsTaskSpec().maxRetries, `is`(3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `#getTask throws IllegalArgumentException when projectId is not set`() {
        SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), HashMap(), false).get()
    }

    @Test
    fun `#onStopedBySystem calls notifier#onSyncStopped`() {
        val inputData = HashMap<String, String>().also {
            it[TaskData.DATA_PROJECT_ID] = "projectId"
        }
        SyncFormsTaskSpec().onStopedBySystem(ApplicationProvider.getApplicationContext(), inputData)
        verify(notifier).onSyncStopped("projectId")
    }

    @Test
    fun `#onStopedBySystem calls formsDataService#lastMatchFormsWithServerFailed`() {
        val inputData = HashMap<String, String>().also {
            it[TaskData.DATA_PROJECT_ID] = "projectId"
        }
        SyncFormsTaskSpec().onStopedBySystem(ApplicationProvider.getApplicationContext(), inputData)
        verify(formsDataService).lastMatchFormsWithServerFailed("projectId")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `#onStopedBySystem throws IllegalArgumentException when projectId is not set`() {
        SyncFormsTaskSpec().onStopedBySystem(ApplicationProvider.getApplicationContext(), HashMap())
    }
}
