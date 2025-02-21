package org.odk.collect.db.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns._ID
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.db.sqlite.CursorExt.foldAndClose
import org.odk.collect.db.sqlite.CursorExt.rowToMap
import org.odk.collect.db.sqlite.RowNumbers.invalidateRowNumbers
import org.odk.collect.db.sqlite.RowNumbers.rawQueryWithRowNumber
import org.odk.collect.db.sqlite.SQLiteColumns.ROW_NUMBER
import org.odk.collect.shared.TempFiles

@RunWith(AndroidJUnit4::class)
class RowNumbersTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `#rawQueryWithRowNumber returns results ordered by row_number column`() {
        val dbConnection = SynchronizedDatabaseConnection(
            context,
            TempFiles.createTempDir().absolutePath,
            "temp.db",
            NoopMigrator(),
            1
        )

        dbConnection.resetTransaction {
            execSQL("CREATE TABLE test_table ($_ID integer PRIMARY KEY, position text)")
        }

        dbConnection.transaction {
            insertOrThrow("test_table", null, ContentValues().also { it.put("position", "first") })
            insertOrThrow("test_table", null, ContentValues().also { it.put("position", "second") })
        }

        val rows =
            dbConnection.rawQueryWithRowNumber("test_table").foldAndClose { it.rowToMap() }
        assertThat(rows.size, equalTo(2))

        assertThat(rows[0]["position"], equalTo("first"))
        assertThat(rows[0][ROW_NUMBER], equalTo("1"))

        assertThat(rows[1]["position"], equalTo("second"))
        assertThat(rows[1][ROW_NUMBER], equalTo("2"))
    }

    @Test
    fun `#rawQueryWithRowNumber returns results ordered by updated row_number column after row deleted and invalidate`() {
        val dbConnection = SynchronizedDatabaseConnection(
            context,
            TempFiles.createTempDir().absolutePath,
            "temp.db",
            NoopMigrator(),
            1
        )

        dbConnection.resetTransaction {
            execSQL("CREATE TABLE test_table ($_ID integer PRIMARY KEY, position text)")
        }

        dbConnection.transaction {
            insertOrThrow("test_table", null, ContentValues().also { it.put("position", "first") })
            insertOrThrow("test_table", null, ContentValues().also { it.put("position", "second") })
            insertOrThrow("test_table", null, ContentValues().also { it.put("position", "third") })
        }

        val beforeRows =
            dbConnection.rawQueryWithRowNumber("test_table").foldAndClose { it.rowToMap() }
        assertThat(beforeRows.size, equalTo(3))

        dbConnection.transaction {
            delete("test_table", "position = ?", arrayOf("second"))
        }

        dbConnection.invalidateRowNumbers("test_table")

        val afterRows =
            dbConnection.rawQueryWithRowNumber("test_table").foldAndClose { it.rowToMap() }
        assertThat(afterRows.size, equalTo(2))

        assertThat(afterRows[0]["position"], equalTo("first"))
        assertThat(afterRows[0][ROW_NUMBER], equalTo("1"))

        assertThat(afterRows[1]["position"], equalTo("third"))
        assertThat(afterRows[1][ROW_NUMBER], equalTo("2"))
    }
}

private class NoopMigrator : DatabaseMigrator {
    override fun onCreate(db: SQLiteDatabase?) {}
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int) {}
}
