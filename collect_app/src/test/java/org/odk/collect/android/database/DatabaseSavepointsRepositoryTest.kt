package org.odk.collect.android.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.odk.collect.android.database.savepoints.DatabaseSavepointsRepository
import org.odk.collect.forms.savepoints.SavepointsRepository
import org.odk.collect.formstest.SavepointsRepositoryTest
import org.odk.collect.shared.TempFiles

@RunWith(AndroidJUnit4::class)
class DatabaseSavepointsRepositoryTest : SavepointsRepositoryTest() {
    override fun buildSubject(cacheDirPath: String, instancesDirPath: String): SavepointsRepository {
        return DatabaseSavepointsRepository(
            ApplicationProvider.getApplicationContext(),
            TempFiles.createTempDir().absolutePath,
            cacheDirPath,
            instancesDirPath
        )
    }
}
