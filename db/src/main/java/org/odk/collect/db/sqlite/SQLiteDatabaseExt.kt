package org.odk.collect.db.sqlite

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder

object SQLiteDatabaseExt {
    fun SQLiteDatabase.query(
        table: String,
        selection: String? = null,
        selectionArgs: Array<String?>? = null
    ): Cursor {
        val qb = SQLiteQueryBuilder().apply {
            tables = table
        }

        return qb.query(this, null, selection, selectionArgs, null, null, null)
    }

    fun SQLiteDatabase.delete(table: String) {
        this.delete(table, null, null)
    }

    @JvmStatic
    fun SQLiteDatabase.doesColumnExist(table: String, column: String): Boolean {
        return SQLiteUtils.getColumnNames(this, table).contains(column)
    }
}
