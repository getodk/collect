package org.odk.collect.android.database

import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.database.instances.DatabaseInstancesRepository
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.formstest.InstancesRepositoryTest
import org.odk.collect.shared.TempFiles.createTempDir
import java.io.File
import java.util.function.Supplier

@RunWith(AndroidJUnit4::class)
class DatabaseInstancesRepositoryTest : InstancesRepositoryTest() {
    private val dbDir = createTempDir()

    override val instancesDir: File = createTempDir()

    override fun buildSubject(): InstancesRepository {
        return DatabaseInstancesRepository(
            ApplicationProvider.getApplicationContext(),
            dbDir.absolutePath,
            instancesDir.absolutePath
        ) { System.currentTimeMillis() }
    }

    override fun buildSubject(clock: Supplier<Long>): InstancesRepository {
        return DatabaseInstancesRepository(
            ApplicationProvider.getApplicationContext(),
            dbDir.absolutePath,
            instancesDir.absolutePath,
            clock
        )
    }

    @Test
    fun save_failsWhenEditOfPointsAtItsOwnDbId() {
        val instancesRepository = buildSubject()

        val originalInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir)
        val originalInstanceDbId = instancesRepository.save(originalInstance)

        val editedInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir, editOf = originalInstanceDbId.dbId + 1)

        assertThrows(SQLiteConstraintException::class.java) {
            instancesRepository.save(editedInstance)
        }
    }

    @Test
    fun save_failsWhenEditOfPointsAtNonExistingDbId() {
        val instancesRepository = buildSubject()

        val originalInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir)
        val originalInstanceDbId = instancesRepository.save(originalInstance)

        val editedInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir, editOf = originalInstanceDbId.dbId + 100)

        assertThrows(SQLiteConstraintException::class.java) {
            instancesRepository.save(editedInstance)
        }
    }
}
