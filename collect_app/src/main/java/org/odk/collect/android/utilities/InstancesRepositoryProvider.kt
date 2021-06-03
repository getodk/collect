package org.odk.collect.android.utilities

import org.odk.collect.android.application.Collect
import org.odk.collect.android.database.instances.DatabaseInstancesRepository
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.instances.InstancesRepository

class InstancesRepositoryProvider {
    fun get(): InstancesRepository {
        val storagePathProvider = StoragePathProvider()

        return DatabaseInstancesRepository(
            Collect.getInstance(),
            storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA),
            storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES),
            System::currentTimeMillis
        )
    }
}
