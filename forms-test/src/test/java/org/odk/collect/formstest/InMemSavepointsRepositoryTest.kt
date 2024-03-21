package org.odk.collect.formstest

import org.odk.collect.forms.savepoints.SavepointsRepository

class InMemSavepointsRepositoryTest : SavepointsRepositoryTest() {
    override fun buildSubject(cacheDirPath: String, instancesDirPath: String): SavepointsRepository {
        return InMemSavepointsRepository()
    }
}
