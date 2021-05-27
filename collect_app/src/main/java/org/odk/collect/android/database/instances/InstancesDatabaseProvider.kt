package org.odk.collect.android.database.instances

import android.content.Context
import org.odk.collect.android.database.DatabaseConnection
import org.odk.collect.android.database.DatabaseConstants

/**
 * Holds "connection" (in this case an instance of [android.database.sqlite.SQLiteOpenHelper]
 * to the Instances database.
 */
class InstancesDatabaseProvider(val context: Context, val path: String) : DatabaseConnection(
    context,
    path,
    DatabaseConstants.INSTANCES_DATABASE_NAME,
    InstanceDatabaseMigrator(),
    DatabaseConstants.INSTANCES_DATABASE_VERSION
)
