package org.odk.collect.androidshared.sqlite

import android.database.Cursor

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

    fun Cursor.getString(column: String): String {
        val columnIndex = this.getColumnIndex(column)
        return this.getString(columnIndex)
    }
}
