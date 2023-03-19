package org.odk.collect.android.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.database.forms.FormDatabaseMigrator
import org.odk.collect.shared.TempFiles.createTempDir
import java.io.File

@RunWith(AndroidJUnit4::class)
class DatabaseConnectionTest {

    @Test
    // https://github.com/getodk/collect/issues/5042
    fun `database file should be recreated if removed between operations`() {
        val dbDir = createTempDir()
        val formsDbPath = dbDir.absolutePath + File.separator + "forms.db"

        DatabaseConnection(
            ApplicationProvider.getApplicationContext(),
            dbDir.absolutePath,
            "forms.db",
            FormDatabaseMigrator(),
            DatabaseConstants.FORMS_DATABASE_VERSION
        ).also {
            it.readableDatabase
            assertTrue(File(formsDbPath).exists())

            File(formsDbPath).delete()
            assertFalse(File(formsDbPath).exists())

            it.readableDatabase
            assertTrue(File(formsDbPath).exists())
        }
    }
}
