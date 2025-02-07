package org.odk.collect.android.mainmenu

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.projects.Project

class CurrentProjectViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val projectsDataService = mock<ProjectsDataService> {
        on { getCurrentProject() } doReturn MutableStateFlow(
            Project.Saved(
                "123",
                "Project X",
                "X",
                "#cccccc"
            )
        )
    }

    @Test
    fun `Initial current project should be set`() {
        val viewModel = CurrentProjectViewModel(projectsDataService)
        assertThat(viewModel.hasCurrentProject(), equalTo(true))
        assertThat(
            viewModel.currentProject.value,
            equalTo(Project.Saved("123", "Project X", "X", "#cccccc"))
        )
    }

    @Test
    fun `setCurrentProject() sets current project`() {
        val project = Project.Saved("456", "Project Y", "Y", "#ffffff")

        val viewModel = CurrentProjectViewModel(projectsDataService)
        viewModel.setCurrentProject(project)
        verify(projectsDataService).setCurrentProject("456")
    }

    @Test
    fun `hasCurrentProject returns false when there is no current project`() {
        whenever(projectsDataService.getCurrentProject()).thenReturn(MutableStateFlow(null))
        val currentProjectViewModel = CurrentProjectViewModel(projectsDataService)

        assertThat(currentProjectViewModel.hasCurrentProject(), equalTo(false))
    }
}
