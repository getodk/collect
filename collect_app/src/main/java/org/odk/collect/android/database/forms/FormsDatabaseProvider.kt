package org.odk.collect.android.database.forms

import org.odk.collect.android.database.DatabaseConnection
import org.odk.collect.android.storage.StoragePathProvider

/**
 * Holds "connection" (in this case an instance of [android.database.sqlite.SQLiteOpenHelper]
 * to the Forms database.
 */
class FormsDatabaseProvider :
    DatabaseConnection({ FormsDatabaseHelper(FormDatabaseMigrator(), StoragePathProvider()) })
