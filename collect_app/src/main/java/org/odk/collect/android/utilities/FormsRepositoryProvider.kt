package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.database.forms.DatabaseFormsRepository
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.FormsRepository

class FormsRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider = StoragePathProvider()
) {

    private val clock = { System.currentTimeMillis() }

    @JvmOverloads
    fun get(projectId: String? = null): FormsRepository {
        val dbPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA, projectId)
        val formsPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, projectId)
        val cachePath = storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId)
        return DatabaseFormsRepository(context, dbPath, formsPath, cachePath, clock)
    }
}
