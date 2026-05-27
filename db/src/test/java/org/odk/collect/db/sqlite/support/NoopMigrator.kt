package org.odk.collect.db.sqlite.support

import android.database.sqlite.SQLiteDatabase
import org.odk.collect.db.sqlite.DatabaseMigrator

class NoopMigrator : DatabaseMigrator {
    override fun onCreate(db: SQLiteDatabase?) {}
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int) {}
}
