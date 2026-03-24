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
        return this.getColumnNames(table).contains(column)
    }

    @JvmStatic
    fun SQLiteDatabase.getColumnNames(table: String): List<String> {
        var columnNames: Array<String>
        this.query(table, null, null, null, null, null, null).use { c ->
            columnNames = c.columnNames
        }

        return columnNames.toList()
    }

    @JvmStatic
    @JvmOverloads
    fun SQLiteDatabase.addColumn(
        table: String,
        column: String,
        type: String,
        default: String? = null
    ) {
        if (default != null) {
            this.execSQL(
                """
                ALTER TABLE "$table" ADD "$column" $type DEFAULT $default;
                """.trimIndent()
            )
        } else {
            this.execSQL(
                """
                ALTER TABLE "$table" ADD "$column" $type;
                """.trimIndent()
            )
        }
    }

    fun SQLiteDatabase.dropTable(table: String) {
        this.execSQL("""DROP TABLE "$table";""")
    }

    fun SQLiteDatabase.renameTable(oldTable: String, newTable: String) {
        this.execSQL("""ALTER TABLE "$oldTable" RENAME TO "$newTable";""")
    }

    fun SQLiteDatabase.copyTableContent(oldTable: String, newTable: String, columns: String) {
        this.execSQL("""INSERT INTO "$newTable" SELECT $columns FROM "$oldTable";""")
    }
}
