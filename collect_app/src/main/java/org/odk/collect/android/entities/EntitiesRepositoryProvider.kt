package org.odk.collect.android.entities

import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.entities.EntitiesRepository
import java.io.File

class EntitiesRepositoryProvider(
    private val projectsDataService: ProjectsDataService,
    private val storagePathProvider: StoragePathProvider
) {

    fun get(projectId: String = projectsDataService.getCurrentProject().uuid): EntitiesRepository {
        val projectDir = File(storagePathProvider.getProjectRootDirPath(projectId))
        return JsonFileEntitiesRepository(projectDir)
    }
}
