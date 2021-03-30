package org.odk.collect.android.projects

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test

abstract class ProjectsRepositoryTest {
    lateinit var projectsRepository: ProjectsRepository

    abstract fun buildSubject(): ProjectsRepository

    @Before
    fun setup() {
        projectsRepository = buildSubject()
    }

    @Test
    fun add_shouldSaveProjectToStorage() {
        projectsRepository.add("Project 1")

        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(1))
        assertThat(projects[0].name, `is`("Project 1"))
    }
}
