package org.odk.collect.db.sqlite

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.db.sqlite.support.NoopMigrator
import org.odk.collect.shared.TempFiles
import java.io.File

@RunWith(AndroidJUnit4::class)
class DatabaseConnectionTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `#withSynchronizedConnection cannot return a Cursor`() {
        val dbConnection = DatabaseConnection(
            context,
            TempFiles.createTempDir().absolutePath,
            "temp.db",
            NoopMigrator(),
            1
        )

        dbConnection.writableDatabase.execSQL("CREATE TABLE blah (id integer);")

        try {
            dbConnection.withSynchronizedConnection {
                readableDatabase.query(
                    "blah",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            }

            fail()
        } catch (_: IllegalStateException) {
            // Expected
        }
    }

    // https://github.com/getodk/collect/issues/5042
    @Test
    fun `database file should be recreated if removed between operations`() {
        val dbDir = TempFiles.createTempDir()
        val dbFileName = "temp.db"
        val dbPath = dbDir.absolutePath + File.separator + dbFileName

        DatabaseConnection(
            context,
            dbDir.absolutePath,
            dbFileName,
            NoopMigrator(),
            1
        ).also {
            it.readableDatabase
            assertTrue(File(dbPath).exists())

            File(dbPath).delete()
            assertFalse(File(dbPath).exists())

            it.readableDatabase
            assertTrue(File(dbPath).exists())
        }
    }
}
