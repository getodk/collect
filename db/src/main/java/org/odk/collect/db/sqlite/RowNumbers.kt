package org.odk.collect.db.sqlite

import android.database.Cursor
import org.odk.collect.db.sqlite.SQLiteColumns.ROW_ID
import org.odk.collect.db.sqlite.SQLiteColumns.ROW_NUMBER

object RowNumbers {
    fun <T> SynchronizedDatabaseConnection.rawQueryWithRowNumber(table: String, selection: String? = null, selectionArgs: Array<String>? = null, cursorMapper: (Cursor) -> T): T {
        this.ensureRowIdTable(table)

        return if (selection != null) {
            this.withConnection {
                val cursor = readableDatabase
                    .rawQuery(
                        """
                        SELECT *, i.$ROW_ID as $ROW_NUMBER
                        FROM "$table" e, "${getRowIdTableName(table)}" i
                        WHERE e._id = i._id AND $selection
                        ORDER BY i.$ROW_ID
                        """.trimIndent(),
                        selectionArgs
                    )

                cursorMapper(cursor)
            }
        } else {
            this.withConnection {
                val cursor = readableDatabase
                    .rawQuery(
                        """
                        SELECT *, i.$ROW_ID as $ROW_NUMBER
                        FROM "$table" e, "${getRowIdTableName(table)}" i
                        WHERE e._id = i._id
                        ORDER BY i.$ROW_ID
                        """.trimIndent(),
                        null
                    )

                cursorMapper(cursor)
            }
        }
    }

    fun SynchronizedDatabaseConnection.invalidateRowNumbers(table: String) {
        this.resetTransaction {
            execSQL(
                """
                DROP TABLE IF EXISTS "${getRowIdTableName(table)}";
                """.trimIndent()
            )
        }
    }

    private fun SynchronizedDatabaseConnection.ensureRowIdTable(table: String) {
        resetTransaction {
            execSQL(
                """
                CREATE TABLE IF NOT EXISTS "${getRowIdTableName(table)}" AS SELECT _id FROM "$table" ORDER BY _id;
                """.trimIndent()
            )
        }
    }

    private fun getRowIdTableName(it: String) = "${it}_row_numbers"
}
