package org.odk.collect.projects

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not
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
    fun `getAll() should return all projects from storage`() {
        projectsRepository.save(projectX)
        projectsRepository.save(projectY)
        projectsRepository.save(projectZ)

        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(3))
        assertThat(projects[0], `is`(projectX.copy(uuid = projects[0].uuid)))
        assertThat(projects[1], `is`(projectY.copy(uuid = projects[1].uuid)))
        assertThat(projects[2], `is`(projectZ.copy(uuid = projects[2].uuid)))
    }

    @Test
    fun `add() should save project to storage`() {
        projectsRepository.save(projectX)

        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(1))
        assertThat(projects[0], `is`(projectX.copy(uuid = projects[0].uuid)))
    }

    @Test
    fun `add() should add uuid if not specified`() {
        projectsRepository.save(projectX)
        assertThat(projectsRepository.getAll()[0].uuid, `is`(not(isEmptyString())))
    }

    @Test
    fun `add() should not add uuid if specified`() {
        projectsRepository.save(projectX.copy(uuid = ""))
        assertThat(projectsRepository.get(""), `is`(projectX.copy(uuid = "")))
    }

    @Test
    fun `add() should update project if already exists`() {
        projectsRepository.save(projectX)
        projectsRepository.save(projectY)
        projectsRepository.save(projectZ)

        val originalProjectX = projectsRepository.getAll()[0]
        val updatedProjectX = originalProjectX.copy(name = "Project X2", "2", "#ff80ff")
        projectsRepository.save(updatedProjectX)

        val projects = projectsRepository.getAll()
        assertThat(projects.size, `is`(3))
        assertThat(projects[0], `is`(updatedProjectX))
        assertThat(projects[1], `is`(projectY.copy(uuid = projects[1].uuid)))
        assertThat(projects[2], `is`(projectZ.copy(uuid = projects[2].uuid)))
    }

    @Test
    fun `delete() should delete project from storage for given uuid`() {
        projectsRepository.save(projectX)
        projectsRepository.save(projectY)

        var projects = projectsRepository.getAll()
        projectsRepository.delete(projects.first { it.name == "ProjectX" }.uuid)
        projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(1))
        assertThat(projects[0], `is`(projectY.copy(uuid = projects[0].uuid)))
    }

    @Test
    fun `deleteAll() should delete all projects from storage`() {
        projectsRepository.save(projectX)
        projectsRepository.save(projectY)

        projectsRepository.deleteAll()
        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(0))
    }
}
