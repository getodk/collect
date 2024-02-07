package org.odk.collect.android.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.odk.collect.android.database.savepoints.SavepointsDatabaseRepository
import org.odk.collect.forms.savepoints.SavepointsRepository
import org.odk.collect.formstest.SavepointsRepositoryTest
import org.odk.collect.shared.TempFiles
import java.io.File

@RunWith(AndroidJUnit4::class)
class DatabaseSavepointsRepositoryTest : SavepointsRepositoryTest() {
    private val cacheDirPath = TempFiles.createTempDir().absolutePath
    private val instancesDirPath = TempFiles.createTempDir().absolutePath

    override fun buildSubject(): SavepointsRepository {
        return SavepointsDatabaseRepository(
            ApplicationProvider.getApplicationContext(),
            TempFiles.createTempDir().absolutePath,
            cacheDirPath,
            instancesDirPath
        )
    }

    override fun getSavepointFile(relativeFilePath: String): File {
        return File(cacheDirPath, relativeFilePath)
    }

    override fun getInstanceFile(relativeFilePath: String): File {
        return File(instancesDirPath, relativeFilePath)
    }
}
