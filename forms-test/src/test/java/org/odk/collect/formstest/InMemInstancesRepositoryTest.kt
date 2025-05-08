package org.odk.collect.formstest

import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.shared.TempFiles.createTempDir
import java.io.File
import java.util.function.Supplier

class InMemInstancesRepositoryTest : InstancesRepositoryTest() {
    override val instancesDir: File = createTempDir()

    override fun buildSubject(): InstancesRepository {
        return InMemInstancesRepository { System.currentTimeMillis() }
    }

    override fun buildSubject(clock: Supplier<Long>): InstancesRepository {
        return InMemInstancesRepository(clock)
    }
}
