package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.database.savepoints.DatabaseSavepointsRepository
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.savepoints.SavepointsRepository

class SavepointsRepositoryProvider(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider
) {

    @JvmOverloads
    fun get(projectId: String? = null): SavepointsRepository {
        val dbPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA, projectId)
        val cachePath = storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId)
        val instancesPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, projectId)

        return DatabaseSavepointsRepository(context, dbPath, cachePath, instancesPath)
    }
}
