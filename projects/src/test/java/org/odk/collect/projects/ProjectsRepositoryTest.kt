package org.odk.collect.projects

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.function.Supplier

abstract class ProjectsRepositoryTest {
    private val projectX = Project.New("ProjectX", "X", "#FF0000")
    private val projectY = Project.New("ProjectY", "Y", "#00FF00")
    private val projectZ = Project.New("ProjectZ", "Z", "#0000FF")

    lateinit var projectsRepository: ProjectsRepository

    abstract fun buildSubject(): ProjectsRepository
    abstract fun buildSubject(clock: Supplier<Long>): ProjectsRepository

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
        assertThat(projects[0], `is`(Project.Saved(projects[0].uuid, projectX)))
        assertThat(projects[1], `is`(Project.Saved(projects[1].uuid, projectY)))
        assertThat(projects[2], `is`(Project.Saved(projects[2].uuid, projectZ)))
    }

    @Test
    fun `getAll() returns projects in created order`() {
        val clock = mock(Supplier::class.java) as Supplier<Long>
        projectsRepository = buildSubject(clock)

        `when`(clock.get()).thenReturn(2)
        projectsRepository.save(projectX)

        `when`(clock.get()).thenReturn(1)
        projectsRepository.save(projectY)

        val projects = projectsRepository.getAll()
        assertThat(projects[0].name, `is`(projectY.name))
        assertThat(projects[1].name, `is`(projectX.name))
    }

    @Test
    fun `save() should save project to storage`() {
        projectsRepository.save(projectX)

        val projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(1))
        assertThat(projects[0], `is`(Project.Saved(projects[0].uuid, projectX)))
    }

    @Test
    fun `save() should add uuid if not specified`() {
        projectsRepository.save(projectX)
        assertThat(projectsRepository.getAll()[0].uuid, `is`(not(isEmptyString())))
    }

    @Test
    fun `save() adds project with uuid if specified`() {
        projectsRepository.save(Project.Saved("blah", projectX))
        assertThat(projectsRepository.getAll()[0].uuid, `is`("blah"))
    }

    @Test
    fun `projects added with uuid are still sorted`() {
        val clock = mock(Supplier::class.java) as Supplier<Long>
        projectsRepository = buildSubject(clock)

        `when`(clock.get()).thenReturn(2)
        projectsRepository.save(Project.Saved("blah1", projectX))

        `when`(clock.get()).thenReturn(1)
        projectsRepository.save(Project.Saved("blah2", projectY))

        assertThat(projectsRepository.getAll()[0].uuid, `is`("blah2"))
        assertThat(projectsRepository.getAll()[1].uuid, `is`("blah1"))
    }

    @Test
    fun `save() should update project if already exists`() {
        projectsRepository.save(projectX)
        projectsRepository.save(projectY)
        projectsRepository.save(projectZ)

        val originalProjectX = projectsRepository.getAll()[0]
        val updatedProjectX = originalProjectX.copy(name = "Project X2", icon = "2", color = "#ff80ff")
        projectsRepository.save(updatedProjectX)

        val projects = projectsRepository.getAll()
        assertThat(projects.size, `is`(3))
        assertThat(projects[0], `is`(updatedProjectX))
        assertThat(projects[1], `is`(Project.Saved(projects[1].uuid, projectY)))
        assertThat(projects[2], `is`(Project.Saved(projects[2].uuid, projectZ)))
    }

    @Test
    fun `updating project does not change its sort order`() {
        val clock = mock(Supplier::class.java) as Supplier<Long>
        projectsRepository = buildSubject(clock)

        `when`(clock.get()).thenReturn(2)
        projectsRepository.save(Project.Saved("blah1", projectX))

        `when`(clock.get()).thenReturn(1)
        val savedProjectY = projectsRepository.save(Project.Saved("blah2", projectY))

        `when`(clock.get()).thenReturn(3)
        projectsRepository.save(savedProjectY)

        assertThat(projectsRepository.getAll()[0].uuid, `is`("blah2"))
        assertThat(projectsRepository.getAll()[1].uuid, `is`("blah1"))
    }

    @Test
    fun `save returns project with id`() {
        val project = projectsRepository.save(projectX)
        assertThat(project.uuid, `is`(notNullValue()))
    }

    @Test
    fun `delete() should delete project from storage for given uuid`() {
        projectsRepository.save(projectX)
        projectsRepository.save(projectY)

        var projects = projectsRepository.getAll()
        projectsRepository.delete(projects.first { it.name == "ProjectX" }.uuid)
        projects = projectsRepository.getAll()

        assertThat(projects.size, `is`(1))
        assertThat(projects[0], `is`(Project.Saved(projects[0].uuid, projectY)))
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
