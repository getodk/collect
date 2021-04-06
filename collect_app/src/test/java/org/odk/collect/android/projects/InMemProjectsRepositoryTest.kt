package org.odk.collect.android.projects

class InMemProjectsRepositoryTest : ProjectsRepositoryTest() {
    override fun buildSubject(): ProjectsRepository {
        return InMemProjectsRepository()
    }
}
