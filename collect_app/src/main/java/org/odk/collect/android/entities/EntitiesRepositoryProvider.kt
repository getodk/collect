package org.odk.collect.android.entities

import android.content.Context
import org.odk.collect.android.database.entities.DatabaseEntitiesRepository
import org.odk.collect.android.storage.StoragePaths
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.projects.ProjectDependencyFactory

class EntitiesRepositoryProvider(
    private val context: Context,
    private val storagePathFactory: ProjectDependencyFactory<StoragePaths>
) :
    ProjectDependencyFactory<EntitiesRepository> {

    override fun create(projectId: String): EntitiesRepository {
        return DatabaseEntitiesRepository(
            context,
            storagePathFactory.create(projectId).metaDir,
            System::currentTimeMillis
        )
    }
}
