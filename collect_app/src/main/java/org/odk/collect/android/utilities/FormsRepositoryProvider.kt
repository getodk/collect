package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.database.forms.DatabaseFormsRepository
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.FormsRepository

class FormsRepositoryProvider(private val context: Context) {

    fun get(): FormsRepository {
        val clock = { System.currentTimeMillis() }
        val storagePathProvider = StoragePathProvider()
        val dbPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA)
        return DatabaseFormsRepository(context, dbPath, clock, storagePathProvider)
    }
}
