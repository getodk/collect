package org.odk.collect.android.database

import android.content.Context
import android.content.ContextWrapper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Allows creating an [SQLiteOpenHelper] that references a `.db` somewhere other than the standard
 * Android database path (by passing this as [Context] to the constructor).
 */
class AltDatabasePathContext(private val path: String, context: Context) : ContextWrapper(context) {

    override fun getDatabasePath(name: String): File {
        return File(path + File.separator + name)
    }

    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?,
        errorHandler: DatabaseErrorHandler?
    ): SQLiteDatabase {
        return openOrCreateDatabase(name, mode, factory)
    }

    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?
    ): SQLiteDatabase {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null)
    }
}
