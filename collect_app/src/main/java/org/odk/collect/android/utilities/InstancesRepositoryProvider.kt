package org.odk.collect.android.utilities

import org.odk.collect.android.application.Collect
import org.odk.collect.android.database.instances.DatabaseInstancesRepository
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.forms.instances.InstancesRepository

class InstancesRepositoryProvider {

    fun get(): InstancesRepository {
        return DatabaseInstancesRepository(DaggerUtils.getComponent(Collect.getInstance()).instancesDatabaseProvider(), StoragePathProvider(), System::currentTimeMillis)
    }
}
