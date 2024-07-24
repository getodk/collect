package org.odk.collect.android.entities

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns._ID
import org.odk.collect.androidshared.sqlite.DatabaseConnection
import org.odk.collect.androidshared.sqlite.DatabaseMigrator
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity

private const val LISTS_TABLE = "lists"
private const val LISTS_NAME = "name"

class DatabaseEntitiesRepository(context: Context, dbPath: String) : EntitiesRepository {

    private val databaseConnection: DatabaseConnection = DatabaseConnection(
        context,
        dbPath,
        "entities.db",
        EntitiesDatabaseMigrator(),
        1,
        true
    )

    override fun save(vararg entities: Entity) {
        entities.forEach {
            val contentValues = ContentValues()
            contentValues.put(LISTS_NAME, it.list)
            databaseConnection.writeableDatabase.insertOrThrow(LISTS_TABLE, null, contentValues)
        }
    }

    override fun getLists(): Set<String> {
        val lists = mutableSetOf<String>()
        databaseConnection.readableDatabase.query(
            LISTS_TABLE,
            arrayOf(LISTS_NAME),
            null,
            null,
            null,
            null,
            null
        ).use {
            it.moveToPosition(-1)
            while (it.moveToNext()) {
                lists.add(it.getString(0))
            }
        }

        return lists
    }

    override fun getEntities(list: String): List<Entity.Saved> {
        return emptyList()
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun addList(list: String) {
        val contentValues = ContentValues()
        contentValues.put(LISTS_NAME, list)
        databaseConnection.writeableDatabase.insertOrThrow(LISTS_TABLE, null, contentValues)
    }

    override fun delete(id: String) {
        TODO("Not yet implemented")
    }

    override fun getById(list: String, id: String): Entity.Saved? {
        TODO("Not yet implemented")
    }

    override fun getAllByProperty(
        list: String,
        property: String,
        value: String
    ): List<Entity.Saved> {
        TODO("Not yet implemented")
    }
}

class EntitiesDatabaseMigrator :
    DatabaseMigrator {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS lists (
                    $_ID integer PRIMARY KEY, 
                    $LISTS_NAME text NOT NULL);
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int) {
        TODO("Not yet implemented")
    }

    override fun onDowngrade(db: SQLiteDatabase?) {
        TODO("Not yet implemented")
    }
}
