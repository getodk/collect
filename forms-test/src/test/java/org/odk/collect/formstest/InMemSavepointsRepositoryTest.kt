package org.odk.collect.formstest

import org.odk.collect.forms.savepoints.SavepointsRepository
import java.io.File

class InMemSavepointsRepositoryTest : SavepointsRepositoryTest() {
    override fun buildSubject(): SavepointsRepository {
        return InMemSavepointsRepository()
    }

    override fun getSavepointFile(relativeFilePath: String): File {
        return File(relativeFilePath)
    }

    override fun getInstanceFile(relativeFilePath: String): File {
        return File(relativeFilePath)
    }
}
