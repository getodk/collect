package org.odk.collect.android.projects

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
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
    fun getAll_shouldReturnAllProjectsFromStorage() {
        projectsRepository.add("ProjectX")
        projectsRepository.add("ProjectY")
        projectsRepository.add("ProjectZ")

        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(3))
        assertThat(projects[0].name, `is`("ProjectX"))
        assertThat(projects[1].name, `is`("ProjectY"))
        assertThat(projects[2].name, `is`("ProjectZ"))
    }

    @Test
    fun add_shouldSaveProjectToStorage() {
        projectsRepository.add("ProjectX")

        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(1))
        assertThat(projects[0].name, `is`("ProjectX"))
    }

    @Test
    fun delete_shouldDeleteProjectFromStorage() {
        projectsRepository.add("ProjectX")
        projectsRepository.add("ProjectY")

        val projects = projectsRepository.getAll()
        projectsRepository.delete(projects.first { it.name == "ProjectX" }.uuid)

        assertThat(projects.size, `is`(1))
        assertThat(projects[0].name, `is`("ProjectY"))
    }

    @Test
    open fun add_shouldAddsUniqueId() {
        projectsRepository.add("ProjectX")
        projectsRepository.add("ProjectY")

        val projects = projectsRepository.getAll()

        assertThat(projects[0].uuid, `is`(notNullValue()))
        assertThat(projects[1].uuid, `is`(notNullValue()))
        assertThat(projects[0].uuid, not(projects[1].uuid))
    }
}
