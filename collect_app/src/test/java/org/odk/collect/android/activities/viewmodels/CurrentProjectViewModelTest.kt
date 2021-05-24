package org.odk.collect.android.activities.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.projects.Project

class CurrentProjectViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var currentProjectProvider: CurrentProjectProvider
    private lateinit var currentProjectViewModel: CurrentProjectViewModel

    @Before
    fun setup() {
        currentProjectProvider = mock(CurrentProjectProvider::class.java)
        `when`(currentProjectProvider.getCurrentProject()).thenReturn(Project.Saved("123", "Project X", "X", "#cccccc"))
        currentProjectViewModel = CurrentProjectViewModel(currentProjectProvider)
    }

    @Test
    fun `Initial current project should be set`() {
        assertThat(currentProjectViewModel.currentProject.value, `is`(Project.Saved("123", "Project X", "X", "#cccccc")))
    }

    @Test
    fun `setCurrentProject() sets current project`() {
        val project = Project.Saved("456", "Project Y", "Y", "#ffffff")

        currentProjectViewModel.setCurrentProject(project)
        verify(currentProjectProvider).setCurrentProject("456")
    }
}
