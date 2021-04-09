package org.odk.collect.android.projects

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test

abstract class ProjectsRepositoryTest {
    private val projectX = Project("ProjectX", "X", "#FF0000")
    private val projectY = Project("ProjectY", "Y", "#00FF00")
    private val projectZ = Project("ProjectZ", "Z", "#0000FF")

    lateinit var projectsRepository: ProjectsRepository

    abstract fun buildSubject(): ProjectsRepository

    @Before
    fun setup() {
        projectsRepository = buildSubject()
    }

    @Test
    fun getAll_shouldReturnAllProjectsFromStorage() {
        projectsRepository.add(projectX)
        projectsRepository.add(projectY)
        projectsRepository.add(projectZ)

        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(3))
        assertThat(projects[0], `is`(projectX.copy(uuid = projects[0].uuid)))
        assertThat(projects[1], `is`(projectY.copy(uuid = projects[1].uuid)))
        assertThat(projects[2], `is`(projectZ.copy(uuid = projects[2].uuid)))
    }

    @Test
    fun add_shouldSaveProjectToStorage() {
        projectsRepository.add(projectX)

        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(1))
        assertThat(projects[0], `is`(projectX.copy(uuid = projects[0].uuid)))
    }

    @Test
    fun delete_shouldDeleteProjectFromStorage() {
        projectsRepository.add(projectX)
        projectsRepository.add(projectY)

        var projects = projectsRepository.getAll()
        projectsRepository.delete(projects.first { it.name == "ProjectX" }.uuid)
        projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(1))
        assertThat(projects[0], `is`(projectY.copy(uuid = projects[0].uuid)))
    }

    @Test
    fun deleteAll_shouldDeleteAllProjectsFromStorage() {
        projectsRepository.add(projectX)
        projectsRepository.add(projectY)

        projectsRepository.deleteAll()
        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(0))
    }

    @Test
    open fun add_shouldAddsUniqueId() {
        projectsRepository.add(projectX)
        projectsRepository.add(projectY)

        val projects = projectsRepository.getAll()

        assertThat(projects[0].uuid, `is`(notNullValue()))
        assertThat(projects[1].uuid, `is`(notNullValue()))
        assertThat(projects[0].uuid, not(projects[1].uuid))
    }
}
