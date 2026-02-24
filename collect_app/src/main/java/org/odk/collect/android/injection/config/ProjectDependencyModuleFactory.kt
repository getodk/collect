package org.odk.collect.android.injection.config

import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.formmanagement.OpenRosaClientProvider
import org.odk.collect.android.projects.FileDebugLogger
import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StoragePaths
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.utilities.SavepointsRepositoryProvider
import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.DebugLogger
import java.io.File
import javax.inject.Inject

class ProjectDependencyModuleFactory @Inject constructor(
    private val settingsProvider: SettingsProvider,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val storagePathProvider: StoragePathProvider,
    private val changeLockProvider: ChangeLockProvider,
    private val openRosaClientProvider: OpenRosaClientProvider,
    private val savepointsRepositoryProvider: SavepointsRepositoryProvider,
    private val entitiesRepositoryProvider: EntitiesRepositoryProvider,
) : ProjectDependencyFactory<ProjectDependencyModule> {
    override fun create(projectId: String): ProjectDependencyModule {
        return ProjectDependencyModule(
            projectId,
            settingsProvider::getUnprotectedSettings,
            formsRepositoryProvider,
            instancesRepositoryProvider,
            storagePathProvider,
            changeLockProvider,
            { openRosaClientProvider.create(projectId) },
            savepointsRepositoryProvider,
            entitiesRepositoryProvider,
            { openRosaClientProvider.create(projectId) },
            DebugLoggerFactory(storagePathProvider)
        )
    }
}

private class DebugLoggerFactory(private val storagePathProvider: ProjectDependencyFactory<StoragePaths>) :
    ProjectDependencyFactory<DebugLogger> {
    override fun create(projectId: String): DebugLogger {
        return FileDebugLogger(File(storagePathProvider.create(projectId).rootDir, "debug.log"))
    }
}
