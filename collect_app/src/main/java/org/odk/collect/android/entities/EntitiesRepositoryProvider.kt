package org.odk.collect.android.entities

import org.odk.collect.android.storage.StoragePaths
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.projects.ProjectDependencyFactory
import java.io.File

class EntitiesRepositoryProvider(private val storagePathFactory: ProjectDependencyFactory<StoragePaths>) :
    ProjectDependencyFactory<EntitiesRepository> {

    override fun create(projectId: String): EntitiesRepository {
        val projectDir = File(storagePathFactory.create(projectId).rootDir)
        return JsonFileEntitiesRepository(projectDir)
    }
}
