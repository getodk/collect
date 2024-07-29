package org.odk.collect.androidshared.sqlite

import android.database.Cursor
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull

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
            if (it.count >= 1) {
                it.moveToPosition(-1)
                it.moveToNext()
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

    fun Cursor.getInt(column: String): Int {
        val columnIndex = this.getColumnIndex(column)
        return this.getInt(columnIndex)
    }

    fun Cursor.getIntOrNull(column: String): Int? {
        val columnIndex = this.getColumnIndex(column)
        return this.getIntOrNull(columnIndex)
    }
}
