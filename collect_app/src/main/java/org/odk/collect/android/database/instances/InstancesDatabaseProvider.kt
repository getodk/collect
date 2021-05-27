package org.odk.collect.android.database.instances

import org.odk.collect.android.database.DatabaseConnection
import org.odk.collect.android.storage.StoragePathProvider

/**
 * Holds "connection" (in this case an instance of [android.database.sqlite.SQLiteOpenHelper]
 * to the Instances database.
 */
class InstancesDatabaseProvider : DatabaseConnection({
    InstancesDatabaseHelper(
        InstanceDatabaseMigrator(),
        StoragePathProvider()
    )
})
