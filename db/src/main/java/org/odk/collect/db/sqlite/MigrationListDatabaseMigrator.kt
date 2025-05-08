package org.odk.collect.db.sqlite

import android.database.sqlite.SQLiteDatabase

abstract class MigrationListDatabaseMigrator(vararg migrations: ((SQLiteDatabase) -> Unit)) :
    DatabaseMigrator {

    private val migrations = migrations.toList()

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int) {
        val migrationsForVersion = if (oldVersion <= 1) {
            migrations
        } else {
            migrations.drop(oldVersion - 1)
        }

        migrationsForVersion.forEach { it(db) }
    }
}
