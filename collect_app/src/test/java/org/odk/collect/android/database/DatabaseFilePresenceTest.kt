package org.odk.collect.android.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.database.forms.DatabaseFormsRepository
import org.odk.collect.shared.TempFiles.createTempDir
import java.io.File

@RunWith(AndroidJUnit4::class)
// https://github.com/getodk/collect/issues/5042
class DatabaseFilePresenceTest {
    private val dbDir = createTempDir()
    private val formsDir = createTempDir()
    private val cacheDir = createTempDir()

    @Test
    fun `database file should be recreated if removed between operations`() {
        val formsDbPath = dbDir.absolutePath + File.separator + "forms.db"

        val repo = DatabaseFormsRepository(
            ApplicationProvider.getApplicationContext(),
            dbDir.absolutePath,
            formsDir.absolutePath,
            cacheDir.absolutePath
        ) { System.currentTimeMillis() }

        repo.all
        assertTrue(File(formsDbPath).exists())

        File(formsDbPath).delete()
        assertFalse(File(formsDbPath).exists())

        repo.all
        assertTrue(File(formsDbPath).exists())
    }
}
