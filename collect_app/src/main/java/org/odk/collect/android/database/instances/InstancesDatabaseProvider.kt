package org.odk.collect.android.database.instances

import org.odk.collect.android.application.Collect
import org.odk.collect.android.database.DatabaseConnection
import org.odk.collect.android.database.DatabaseConstants
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory

/**
 * Holds "connection" (in this case an instance of [android.database.sqlite.SQLiteOpenHelper]
 * to the Instances database.
 */
class InstancesDatabaseProvider : DatabaseConnection(
    Collect.getInstance(),
    StoragePathProvider().getOdkDirPath(StorageSubdirectory.METADATA),
    DatabaseConstants.INSTANCES_DATABASE_NAME,
    InstanceDatabaseMigrator(),
    DatabaseConstants.INSTANCES_DATABASE_VERSION
)
