package org.odk.collect.android.utilities

import org.odk.collect.android.application.Collect
import org.odk.collect.android.database.forms.DatabaseFormsRepository
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.forms.FormsRepository

class FormsRepositoryProvider {

    fun get(): FormsRepository {
        return DatabaseFormsRepository({ System.currentTimeMillis() }, StoragePathProvider(), DaggerUtils.getComponent(Collect.getInstance()).formsDatabaseProvider())
    }
}
