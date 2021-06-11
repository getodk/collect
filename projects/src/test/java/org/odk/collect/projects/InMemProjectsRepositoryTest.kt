package org.odk.collect.projects

import org.odk.collect.shared.strings.UUIDGenerator

class InMemProjectsRepositoryTest : ProjectsRepositoryTest() {
    override fun buildSubject(): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator())
    }
}
