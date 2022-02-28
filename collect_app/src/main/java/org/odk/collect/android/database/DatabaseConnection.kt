package org.odk.collect.android.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import timber.log.Timber

/**
 * Allows access to a database file. The actual underlying connection (an instance of
 * [SQLiteOpenHelper] to this database will be reused for different instances of
 * [DatabaseConnection] that refer to the same file.
 *
 * @param migrator used to migrate or create the database automatically before access
 */
open class DatabaseConnection(
    private val context: Context,
    private val path: String,
    private val name: String,
    private val migrator: DatabaseMigrator,
    private val databaseVersion: Int
) {

    val writeableDatabase: SQLiteDatabase
        get() = dbHelper.writableDatabase
    val readableDatabase: SQLiteDatabase
        get() = dbHelper.readableDatabase

    private val dbHelper: SQLiteOpenHelper by lazy {
        getOpenHelper(path + name) {
            DatabaseMigratorSQLiteOpenHelper(
                AltDatabasePathContext(path, context),
                name,
                null,
                databaseVersion,
                migrator
            )
        }
    }

    companion object {

        private val openHelpers = mutableMapOf<String, SQLiteOpenHelper>()

        private fun getOpenHelper(
            name: String,
            helperFactory: () -> SQLiteOpenHelper
        ): SQLiteOpenHelper {
            return openHelpers.getOrPut(name, helperFactory)
        }

        @JvmStatic
        fun closeAll() {
            openHelpers.forEach { (_, openHelper) -> openHelper.close() }
            openHelpers.clear()
        }
    }
}

/**
 * [SQLiteOpenHelper] that delegates `onCreate`, `onUpdate`, `onDowngrade` to a [DatabaseMigrator].
 */
private class DatabaseMigratorSQLiteOpenHelper(
    context: Context,
    name: String,
    cursorFactory: CursorFactory?,
    version: Int,
    private val databaseMigrator: DatabaseMigrator
) : SQLiteOpenHelper(context, name, cursorFactory, version) {

    override fun onCreate(db: SQLiteDatabase?) {
        databaseMigrator.onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion)
        databaseMigrator.onUpgrade(db, oldVersion)
        Timber.i(
            "Upgrading database from version %d to %d completed with success.",
            oldVersion,
            newVersion
        )
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Timber.i("Downgrading database from version %d to %d", oldVersion, newVersion)
        databaseMigrator.onDowngrade(db)
        Timber.i(
            "Downgrading database from %d to %d completed with success.",
            oldVersion,
            newVersion
        )
    }
}
