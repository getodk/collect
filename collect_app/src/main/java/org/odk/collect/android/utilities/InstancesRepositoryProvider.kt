package org.odk.collect.android.utilities

import org.odk.collect.android.application.Collect
import org.odk.collect.android.database.DatabaseInstancesRepository
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.instances.InstancesRepository
import org.odk.collect.android.storage.StoragePathProvider

class InstancesRepositoryProvider {

    fun get(): InstancesRepository {
        return DatabaseInstancesRepository(DaggerUtils.getComponent(Collect.getInstance()).instancesDatabaseProvider(), StoragePathProvider())
    }
}
