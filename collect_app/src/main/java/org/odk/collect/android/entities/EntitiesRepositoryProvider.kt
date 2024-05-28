package org.odk.collect.android.entities

import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
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

    @Deprecated("Creating dependency without specified project is dangerous")
    fun create(): EntitiesRepository {
        val currentProject =
            DaggerUtils.getComponent(Collect.getInstance()).currentProjectProvider()
                .getCurrentProject()
        return create(currentProject.uuid)
    }
}
