package org.odk.collect.android.entities

import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.projects.ProjectDependencyFactory
import java.io.File

class EntitiesRepositoryProvider(private val storagePathProvider: StoragePathProvider) :
    ProjectDependencyFactory<EntitiesRepository> {

    override fun create(projectId: String): EntitiesRepository {
        val projectDir = File(storagePathProvider.getProjectRootDirPath(projectId))
        return JsonFileEntitiesRepository(projectDir)
    }
}
