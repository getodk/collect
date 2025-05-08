package org.odk.collect.db.sqlite

import android.database.sqlite.SQLiteDatabase

abstract class MigrationListDatabaseMigrator(private val databaseVersion: Int, vararg migrations: ((SQLiteDatabase) -> Unit)) :
    DatabaseMigrator {

    private val migrations = migrations.toList()

    override fun onCreate(db: SQLiteDatabase) {
        createDbForVersion(db, databaseVersion)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int) {
        val migrationsForVersion = if (oldVersion <= 1) {
            migrations
        } else {
            migrations.drop(oldVersion - 1)
        }

        migrationsForVersion.forEach { it(db) }
    }

    abstract fun createDbForVersion(db: SQLiteDatabase, version: Int)
}
