package org.odk.collect.android.projects

import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.formmanagement.FormSourceProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.utilities.SavepointsRepositoryProvider
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.forms.savepoints.SavepointsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.locks.ChangeLock
import org.odk.collect.shared.settings.Settings

/**
 * Provides all the basic/building block dependencies needed when performing logic inside a
 * project.
 */
data class ProjectDependencyModule(
    val projectId: String,
    private val settingsFactory: ProjectDependencyFactory<Settings>,
    private val formsRepositoryFactory: ProjectDependencyFactory<FormsRepository>,
    private val instancesRepositoryProvider: ProjectDependencyFactory<InstancesRepository>,
    private val storagePathsFactory: ProjectDependencyFactory<StoragePaths>,
    private val changeLockFactory: ProjectDependencyFactory<ChangeLocks>,
    private val formSourceFactory: ProjectDependencyFactory<FormSource>,
    private val savepointsRepositoryFactory: ProjectDependencyFactory<SavepointsRepository>,
    private val entitiesRepositoryFactory: ProjectDependencyFactory<EntitiesRepository>
) {
    val generalSettings by lazy { settingsFactory.create(projectId) }
    val formsRepository by lazy { formsRepositoryFactory.create(projectId) }
    val instancesRepository by lazy { instancesRepositoryProvider.create(projectId) }
    val formSource by lazy { formSourceFactory.create(projectId) }
    val formsLock by lazy { changeLockFactory.create(projectId).formsLock }
    val instancesLock by lazy { changeLockFactory.create(projectId).instancesLock }
    val formsDir by lazy { storagePathsFactory.create(projectId).formsDir }
    val cacheDir by lazy { storagePathsFactory.create(projectId).cacheDir }
    val entitiesRepository by lazy { entitiesRepositoryFactory.create(projectId) }
    val savepointsRepository by lazy { savepointsRepositoryFactory.create(projectId) }
    val rootDir by lazy { storagePathsFactory.create(projectId).rootDir }
    val instancesDir by lazy { storagePathsFactory.create(projectId).instancesDir }
}

class ProjectDependencyModuleFactory(
    private val settingsProvider: SettingsProvider,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val storagePathProvider: StoragePathProvider,
    private val changeLockProvider: ChangeLockProvider,
    private val formSourceProvider: FormSourceProvider,
    private val savepointsRepositoryProvider: SavepointsRepositoryProvider,
    private val entitiesRepositoryProvider: EntitiesRepositoryProvider,
) : ProjectDependencyFactory<ProjectDependencyModule> {
    override fun create(projectId: String): ProjectDependencyModule {
        return ProjectDependencyModule(
            projectId,
            ProjectDependencyFactory.from { settingsProvider.getUnprotectedSettings(projectId) },
            ProjectDependencyFactory.from { formsRepositoryProvider.get(projectId) },
            ProjectDependencyFactory.from { instancesRepositoryProvider.get(projectId) },
            ProjectDependencyFactory.from {
                StoragePaths(
                    storagePathProvider.getProjectRootDirPath(projectId),
                    storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, projectId),
                    storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, projectId),
                    storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId)
                )
            },
            ProjectDependencyFactory.from {
                ChangeLocks(
                    changeLockProvider.getFormLock(projectId),
                    changeLockProvider.getInstanceLock(projectId)
                )
            },
            ProjectDependencyFactory.from { formSourceProvider.get(projectId) },
            ProjectDependencyFactory.from { savepointsRepositoryProvider.get(projectId) },
            ProjectDependencyFactory.from { entitiesRepositoryProvider.get(projectId) }
        )
    }
}

data class StoragePaths(
    val rootDir: String,
    val formsDir: String,
    val instancesDir: String,
    val cacheDir: String
)

data class ChangeLocks(val formsLock: ChangeLock, val instancesLock: ChangeLock)

interface ProjectDependencyFactory<T> {
    fun create(projectId: String): T

    companion object {
        fun <T> from(factory: (String) -> T): ProjectDependencyFactory<T> {
            return object : ProjectDependencyFactory<T> {
                override fun create(projectId: String): T {
                    return factory(projectId)
                }
            }
        }
    }
}
