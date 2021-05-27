package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.database.forms.DatabaseFormsRepository
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.FormsRepository

class FormsRepositoryProvider(private val context: Context) {

    private val clock = { System.currentTimeMillis() }
    private val storagePathProvider = StoragePathProvider()

    fun get(): FormsRepository {
        val dbPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA)
        return DatabaseFormsRepository(context, dbPath, clock, storagePathProvider)
    }
}
