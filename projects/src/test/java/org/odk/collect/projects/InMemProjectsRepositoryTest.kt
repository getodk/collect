package org.odk.collect.projects

import org.odk.collect.shared.strings.UUIDGenerator
import java.util.function.Supplier

class InMemProjectsRepositoryTest : ProjectsRepositoryTest() {
    override fun buildSubject(): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator(),)
    }

    override fun buildSubject(clock: Supplier<Long>): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator(), clock)
    }
}
