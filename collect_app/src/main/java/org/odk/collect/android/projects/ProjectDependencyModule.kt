package org.odk.collect.android.projects

import org.odk.collect.android.storage.StoragePaths
import org.odk.collect.android.utilities.ChangeLocks
import org.odk.collect.entities.server.EntitySource
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.forms.savepoints.SavepointsRepository
import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.projects.projectDependency
import org.odk.collect.shared.DebugLogger
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
    private val entitiesRepositoryFactory: ProjectDependencyFactory<EntitiesRepository>,
    private val entitySourceFactory: ProjectDependencyFactory<EntitySource>,
    private val debugLoggerFactory: ProjectDependencyFactory<DebugLogger>
) {
    val generalSettings by projectDependency(projectId, settingsFactory)
    val formsRepository by projectDependency(projectId, formsRepositoryFactory)
    val instancesRepository by projectDependency(projectId, instancesRepositoryProvider)
    val formSource by projectDependency(projectId, formSourceFactory)
    val formsLock by projectDependency(projectId, changeLockFactory) { it.formsLock }
    val instancesLock by projectDependency(projectId, changeLockFactory) { it.instancesLock }
    val formsDir by projectDependency(projectId, storagePathsFactory) { it.formsDir }
    val cacheDir by projectDependency(projectId, storagePathsFactory) { it.cacheDir }
    val entitiesRepository by projectDependency(projectId, entitiesRepositoryFactory)
    val savepointsRepository by projectDependency(projectId, savepointsRepositoryFactory)
    val rootDir by projectDependency(projectId, storagePathsFactory) { it.rootDir }
    val instancesDir by projectDependency(projectId, storagePathsFactory) { it.instancesDir }
    val entitySource by projectDependency(projectId, entitySourceFactory)
    val debugLogger by projectDependency(projectId, debugLoggerFactory)
}
