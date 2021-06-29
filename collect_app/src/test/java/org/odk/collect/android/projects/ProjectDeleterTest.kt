package org.odk.collect.android.projects

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class ProjectDeleterTest {
    private val project1 = Project.Saved("1", "1", "1", "#ffffff")
    private val project2 = Project.Saved("2", "2", "2", "#cccccc")
    private val currentProjectProvider = mock<CurrentProjectProvider> {
        on { getCurrentProject() } doReturn project1
    }
    private val formUpdateManager = mock<FormUpdateScheduler>()
    private val instanceSubmitScheduler = mock<InstanceSubmitScheduler>()
    private val instancesRepository = spy(InMemInstancesRepository())

    @Test
    fun `ProjectDeleter should look for forms with proper statuses`() {
        val deleter = ProjectDeleter(
            mock(),
            currentProjectProvider,
            formUpdateManager,
            instanceSubmitScheduler,
            instancesRepository
        )

        deleter.deleteCurrentProject()

        verify(instancesRepository)
            .getAllByStatus(
                Instance.STATUS_INCOMPLETE,
                Instance.STATUS_COMPLETE,
                Instance.STATUS_SUBMISSION_FAILED
            )

        verifyNoMoreInteractions(instancesRepository)
    }

    @Test
    fun `If there are unsent instances the project should not be deleted`() {
        instancesRepository.save(
            Instance.Builder()
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

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
    fun `If there are saved instances but all sent the project should be deleted`() {
        instancesRepository.save(
            Instance.Builder()
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )

        val deleter = ProjectDeleter(
            mock(),
            currentProjectProvider,
            formUpdateManager,
            instanceSubmitScheduler,
            instancesRepository
        )

        val result = deleter.deleteCurrentProject()
        assertThat(result, instanceOf(DeleteProjectResult.DeletedSuccessfully::class.java))
        assertThat((result as DeleteProjectResult.DeletedSuccessfully).newCurrentProject, `is`(nullValue()))
    }

    @Test
    fun `Deleting project cancels scheduled form updates and instance submits`() {
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
        assertThat((result as DeleteProjectResult.DeletedSuccessfully).newCurrentProject, `is`(nullValue()))
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
        assertThat((result as DeleteProjectResult.DeletedSuccessfully).newCurrentProject, `is`(project2))
    }
}
