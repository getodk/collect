package org.odk.collect.android.utilities

import org.odk.collect.android.application.Collect
import org.odk.collect.android.database.instances.DatabaseInstancesRepository
import org.odk.collect.android.database.instances.InstancesDatabaseProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.instances.InstancesRepository

class InstancesRepositoryProvider {
    fun get(): InstancesRepository {
        return DatabaseInstancesRepository(
            InstancesDatabaseProvider(
                Collect.getInstance(),
                StoragePathProvider().getOdkDirPath(StorageSubdirectory.METADATA)
            ),
            StoragePathProvider(), System::currentTimeMillis
        )
    }
}
