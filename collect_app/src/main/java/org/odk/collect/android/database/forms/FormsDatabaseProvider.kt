package org.odk.collect.android.database.forms

import android.content.Context
import org.odk.collect.android.database.DatabaseConnection
import org.odk.collect.android.database.DatabaseConstants

/**
 * Holds "connection" (in this case an instance of [android.database.sqlite.SQLiteOpenHelper]
 * to the Forms database.
 */
class FormsDatabaseProvider(val context: Context, val path: String) : DatabaseConnection(
    context,
    path,
    DatabaseConstants.FORMS_DATABASE_NAME,
    FormDatabaseMigrator(),
    DatabaseConstants.FORMS_DATABASE_VERSION
)
