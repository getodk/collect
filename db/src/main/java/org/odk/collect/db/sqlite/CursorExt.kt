package org.odk.collect.db.sqlite

import android.database.Cursor
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import org.odk.collect.db.sqlite.CursorExt.getInt
import org.odk.collect.db.sqlite.CursorExt.getLong

object CursorExt {
    fun <T> Cursor.foldAndClose(initial: T, operation: (T, Cursor) -> T): T {
        return this.use {
            it.moveToPosition(-1)

            var accumulator = initial
            while (it.moveToNext()) {
                accumulator = operation(accumulator, it)
            }

            accumulator
        }
    }

    fun <T> Cursor.foldAndClose(operation: (Cursor) -> T): List<T> {
        return this.use {
            it.moveToPosition(-1)

            val accumulator = ArrayList<T>(it.count)
            while (it.moveToNext()) {
                accumulator.add(operation(it))
            }

            accumulator
        }
    }

    fun <T> Cursor.first(map: (Cursor) -> T): T? {
        return this.use {
            if (it.moveToFirst()) {
                map(it)
            } else {
                return null
            }
        }
    }

    fun Cursor.getString(column: String): String {
        val columnIndex = this.getColumnIndex(column)
        return this.getString(columnIndex)
    }

    fun Cursor.getStringOrNull(column: String): String? {
        val columnIndex = this.getColumnIndex(column)
        return this.getStringOrNull(columnIndex)
    }

    fun Cursor.getLong(column: String): Long {
        val columnIndex = this.getColumnIndex(column)
        return this.getLong(columnIndex)
    }

    fun Cursor.getLongOrNull(column: String): Long? {
        val columnIndex = this.getColumnIndex(column)
        return this.getLongOrNull(columnIndex)
    }

    fun Cursor.getInt(column: String): Int {
        val columnIndex = this.getColumnIndex(column)
        return this.getInt(columnIndex)
    }

    fun Cursor.getIntOrNull(column: String): Int? {
        val columnIndex = this.getColumnIndex(column)
        return this.getIntOrNull(columnIndex)
    }

    /**
     * Translates Integer column to Boolean as described [here](https://sqlite.org/datatype3.html).
     */
    fun Cursor.getBoolean(column: String): Boolean {
        val columnIndex = this.getColumnIndex(column)
        return this.getInt(columnIndex) == 1
    }

    /**
     * Prevents doing repeated [Cursor.getColumnIndex] lookups and also works around the lack of
     * support for column names including a "." there (due to the mysterious bug 903852).
     *
     * @see [SQLiteCursor source](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/database/sqlite/SQLiteCursor.java;l=178?q=sqlitecursor)
     */
    fun Cursor.rowToMap(): Map<String, String> {
        return this.columnNames.foldIndexed(mutableMapOf()) { index, map, column ->
            map[column] = this.getString(index)
            map
        }
    }
}
