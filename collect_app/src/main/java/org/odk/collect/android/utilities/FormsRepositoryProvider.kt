package org.odk.collect.android.utilities

import org.odk.collect.android.database.forms.DatabaseFormsRepository
import org.odk.collect.android.database.forms.FormsDatabaseProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.forms.FormsRepository

class FormsRepositoryProvider {

    fun get(): FormsRepository {
        return DatabaseFormsRepository({ System.currentTimeMillis() }, StoragePathProvider(), FormsDatabaseProvider())
    }
}
