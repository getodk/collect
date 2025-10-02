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
        databaseVersion,
        true
    )

    fun <T> withConnection(block: DatabaseConnection.() -> T): T {
        return databaseConnection.withSynchronizedConnection(block)
    }

    fun transaction(
        body: SQLiteDatabase.() -> Unit
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
    fun resetTransaction(
        body: SQLiteDatabase.() -> Unit
    ) {
        transaction(body)
        databaseConnection.reset()
    }
}
