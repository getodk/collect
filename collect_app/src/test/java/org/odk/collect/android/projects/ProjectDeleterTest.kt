package org.odk.collect.android.projects

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.android.database.instances.DatabaseInstancesRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class ProjectDeleterTest {
    private val project1 = Project.Saved("1", "1", "1", "#ffffff")
    private val project2 = Project.Saved("2", "2", "2", "#cccccc")
    private val currentProjectProvider = mock<CurrentProjectProvider> {
        on { getCurrentProject() } doReturn project1
    }
    private val instancesRepository = mock<DatabaseInstancesRepository> {
        on { allUnsent } doReturn emptyList()
    }

    @Test
    fun `If there are unsent instances the project should not be deleted`() {
        val instancesRepository = mock<DatabaseInstancesRepository> {
            on { allUnsent } doReturn listOf(Instance.Builder().build())
        }

        val deleter = ProjectDeleter(
            mock(),
            mock(),
            mock(),
            mock(),
            instancesRepository
        )

        deleter.deleteCurrentProject()
        assertThat(deleter.deleteCurrentProject(), instanceOf(DeleteProjectResult.UnsentInstances::class.java))
    }

    @Test
    fun `Deleting project cancels scheduled form updates and instance submits`() {
        val formUpdateManager = mock<FormUpdateScheduler>()
        val instanceSubmitScheduler = mock<InstanceSubmitScheduler>()

        val deleter = ProjectDeleter(
            mock(),
            currentProjectProvider,
            formUpdateManager,
            instanceSubmitScheduler,
            instancesRepository
        )

        deleter.deleteCurrentProject()
        verify(formUpdateManager).cancelUpdates(project1.uuid)
        verify(instanceSubmitScheduler).cancelSubmit(project1.uuid)
    }

    @Test
    fun `Deleting project deletes it from projects repository`() {
        val projectsRepository = mock<ProjectsRepository>()

        val deleter = ProjectDeleter(
            projectsRepository,
            currentProjectProvider,
            mock(),
            mock(),
            instancesRepository
        )

        deleter.deleteCurrentProject()
        verify(projectsRepository).delete(project1.uuid)
    }

    @Test
    fun `If the deleted project was the last one return DeletedSuccessfully with null parameter`() {
        val deleter = ProjectDeleter(
            mock(),
            currentProjectProvider,
            mock(),
            mock(),
            instancesRepository
        )

        val result = deleter.deleteCurrentProject()
        assertThat(result, instanceOf(DeleteProjectResult.DeletedSuccessfully::class.java))
        assertThat((result as DeleteProjectResult.DeletedSuccessfully).project, `is`(nullValue()))
    }

    @Test
    fun `If the deleted project was not the last one set the current project and return the new current one`() {
        val projectsRepository = mock<ProjectsRepository> {
            on { getAll() } doReturn listOf(project2)
        }

        val deleter = ProjectDeleter(
            projectsRepository,
            currentProjectProvider,
            mock(),
            mock(),
            instancesRepository
        )

        val result = deleter.deleteCurrentProject()
        verify(currentProjectProvider).setCurrentProject(project2.uuid)
        assertThat(result, instanceOf(DeleteProjectResult.DeletedSuccessfully::class.java))
        assertThat((result as DeleteProjectResult.DeletedSuccessfully).project, `is`(project2))
    }
}
