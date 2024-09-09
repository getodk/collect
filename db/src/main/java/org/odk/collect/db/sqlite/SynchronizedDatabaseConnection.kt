package org.odk.collect.db.sqlite

import android.content.Context

class SynchronizedDatabaseConnection constructor(
    context: Context,
    path: String,
    name: String,
    migrator: DatabaseMigrator,
    databaseVersion: Int
) {
    private val databaseConnection = DatabaseConnection(
        context,
        path,
        name,
        migrator,
        databaseVersion
    )

    fun <T> withConnection(block: DatabaseConnection.() -> T): T {
        return databaseConnection.withSynchronizedConnection(block)
    }
}
