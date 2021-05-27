package org.odk.collect.android.database.forms

import org.odk.collect.android.application.Collect
import org.odk.collect.android.database.DatabaseConnection
import org.odk.collect.android.database.DatabaseConstants
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory

/**
 * Holds "connection" (in this case an instance of [android.database.sqlite.SQLiteOpenHelper]
 * to the Forms database.
 */
class FormsDatabaseProvider : DatabaseConnection(
    Collect.getInstance(),
    StoragePathProvider().getOdkDirPath(StorageSubdirectory.METADATA),
    DatabaseConstants.FORMS_DATABASE_NAME,
    FormDatabaseMigrator(),
    DatabaseConstants.FORMS_DATABASE_VERSION
)
