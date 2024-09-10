package org.odk.collect.db.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction

class SynchronizedDatabaseConnection(
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

    fun <T> transaction(
        body: SQLiteDatabase.() -> T
    ) {
        return withConnection {
            writableDatabase.transaction {
                body()
            }
        }
    }

    /**
     * Runs a transaction and then calls [DatabaseConnection.reset]. Useful for transactions
     * that will mutate the DB schema.
     */
    fun <T> resetTransaction(
        body: SQLiteDatabase.() -> T
    ) {
        transaction(body)
        databaseConnection.reset()
    }
}
