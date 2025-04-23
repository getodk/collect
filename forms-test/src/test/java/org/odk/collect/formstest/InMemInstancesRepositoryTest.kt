package org.odk.collect.formstest

import org.junit.Before
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.shared.TempFiles.createTempDir
import java.util.function.Supplier

class InMemInstancesRepositoryTest : InstancesRepositoryTest() {
    private lateinit var tempDirectory: String

    @Before
    fun setup() {
        tempDirectory = createTempDir().absolutePath
    }

    override fun buildSubject(): InstancesRepository {
        return InMemInstancesRepository { System.currentTimeMillis() }
    }

    override fun buildSubject(clock: Supplier<Long>): InstancesRepository {
        return InMemInstancesRepository(clock)
    }

    override fun getInstancesDir(): String {
        return tempDirectory
    }
}
