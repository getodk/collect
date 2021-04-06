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
        projectsRepository.add("ProjectX", "X")
        projectsRepository.add("ProjectY", "y")
        projectsRepository.add("ProjectZ", "Z")

        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(3))
        assertProject(projects[0], "ProjectX", "X")
        assertProject(projects[1], "ProjectY", "y")
        assertProject(projects[2], "ProjectZ", "Z")
    }

    @Test
    fun add_shouldSaveProjectToStorage() {
        projectsRepository.add("ProjectX", "X")

        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(1))
        assertProject(projects[0], "ProjectX", "X")
    }

    @Test
    fun delete_shouldDeleteProjectFromStorage() {
        projectsRepository.add("ProjectX", "X")
        projectsRepository.add("ProjectY", "Y")

        val projects = projectsRepository.getAll()
        projectsRepository.delete(projects.first { it.name == "ProjectX" }.uuid)

        assertThat(projects.size, `is`(1))
        assertProject(projects[0], "ProjectY", "Y")
    }

    @Test
    open fun add_shouldAddsUniqueId() {
        projectsRepository.add("ProjectX", "X")
        projectsRepository.add("ProjectY", "Y")

        val projects = projectsRepository.getAll()

        assertThat(projects[0].uuid, `is`(notNullValue()))
        assertThat(projects[1].uuid, `is`(notNullValue()))
        assertThat(projects[0].uuid, not(projects[1].uuid))
    }

    private fun assertProject(project: Project, projectName: String, projectIcon: String) {
        assertThat(project.name, `is`(projectName))
        assertThat(project.icon, `is`(projectIcon))
    }
}
