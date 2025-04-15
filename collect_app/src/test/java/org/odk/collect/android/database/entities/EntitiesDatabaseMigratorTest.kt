package org.odk.collect.android.database.entities

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.db.sqlite.CursorExt.foldAndClose
import org.odk.collect.db.sqlite.CursorExt.rowToMap
import org.odk.collect.db.sqlite.SQLiteDatabaseExt.query

@RunWith(AndroidJUnit4::class)
class EntitiesDatabaseMigratorTest {

    /**
     * Version 1 was only available in beta and upgrading from 1 to 2 originally just cleared
     * all data. If there are any version 1s left in the wild, they will get a crash on launch.
     */
    @Test(expected = IllegalStateException::class)
    fun `#onUpdate from version 1`() {
        val db = SQLiteDatabase.create(null)
        val migrator = EntitiesDatabaseMigrator()
        migrator.onUpgrade(db, 1)
    }

    @Test
    fun `#onUpgrade from version 2`() {
        val db = SQLiteDatabase.create(null)
        val migrator = EntitiesDatabaseMigrator()
        migrator.createDbForVersion(db, 2)

        val listContentValues = ContentValues().also {
            it.put("name", "blah")
            it.put("hash", "somehash")
        }
        db.insert("lists", null, listContentValues)

        migrator.onUpgrade(db, 2)
        val lists = db.query("lists").foldAndClose { it.rowToMap() }
        assertThat(lists.size, equalTo(1))
        assertThat(lists[0]["name"], equalTo("blah"))
        assertThat(lists[0]["hash"], equalTo("somehash"))
        assertThat(lists[0]["needs_approval"], equalTo("0"))
    }
}
