package org.odk.collect.db.sqlite

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.db.sqlite.support.NoopMigrator
import org.odk.collect.shared.TempFiles

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
}
