package org.odk.collect.android.database

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

open class DatabaseConnection(private val helperCreator: () -> SQLiteOpenHelper) {

    val writeableDatabase: SQLiteDatabase
        get() = dbHelper.writableDatabase
    val readableDatabase: SQLiteDatabase
        get() = dbHelper.readableDatabase

    private val dbHelper: SQLiteOpenHelper by lazy {
        helperCreator().also { registerConnection(it) }
    }

    companion object {

        private val connections = mutableListOf<SQLiteOpenHelper>()

        private fun registerConnection(connection: SQLiteOpenHelper) {
            connections.add(connection)
        }

        @JvmStatic
        fun closeAll() {
            connections.forEach { it.close() }
            connections.clear()
        }
    }
}
