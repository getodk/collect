package org.odk.collect.android.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.odk.collect.android.database.instances.DatabaseInstancesRepository
import org.odk.collect.forms.instances.InstancesRepository
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
}
