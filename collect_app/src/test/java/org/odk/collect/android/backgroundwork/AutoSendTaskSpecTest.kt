package org.odk.collect.android.backgroundwork

import android.app.Application
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
import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.openrosa.OpenRosaHttpInterface
import org.odk.collect.android.projects.ProjectDependencyProviderFactory
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.utilities.SavepointsRepositoryProvider
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class AutoSendTaskSpecTest {

    private val instancesDataService = mock<InstancesDataService>()
    private lateinit var projectId: String

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesInstancesDataService(
                application: Application?,
                instancesRepositoryProvider: InstancesRepositoryProvider?,
                projectsDataService: ProjectsDataService?,
                formsRepositoryProvider: FormsRepositoryProvider?,
                entitiesRepositoryProvider: EntitiesRepositoryProvider?,
                storagePathProvider: StoragePathProvider?,
                instanceSubmitScheduler: InstanceSubmitScheduler?,
                savepointsRepositoryProvider: SavepointsRepositoryProvider?,
                changeLockProvider: ChangeLockProvider?,
                projectsDependencyProviderFactory: ProjectDependencyProviderFactory?,
                notifier: Notifier?,
                propertyManager: PropertyManager?,
                httpInterface: OpenRosaHttpInterface
            ): InstancesDataService {
                return instancesDataService
            }
        })

        RobolectricHelpers.mountExternalStorage()
        projectId = CollectHelpers.setupDemoProject()
    }

    @Test
    fun `passes projectDependencyProvider with proper project id`() {
        val inputData = mapOf(TaskData.DATA_PROJECT_ID to projectId)
        AutoSendTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, true).get()
        verify(instancesDataService).autoSendInstances(projectId)
    }

    @Test
    fun `maxRetries should not be limited`() {
        assertThat(AutoSendTaskSpec().maxRetries, `is`(nullValue()))
    }
}
