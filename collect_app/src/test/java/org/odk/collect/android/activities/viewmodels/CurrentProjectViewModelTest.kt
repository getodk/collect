package org.odk.collect.android.activities.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.application.initialization.AnalyticsInitializer
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.projects.Project

class CurrentProjectViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val currentProjectProvider = mock<CurrentProjectProvider> {
        on { getCurrentProject() } doReturn Project.Saved("123", "Project X", "X", "#cccccc")
    }

    private val analyticsInitializer = mock<AnalyticsInitializer>()
    private val currentProjectViewModel = CurrentProjectViewModel(
        currentProjectProvider,
        analyticsInitializer
    )

    @Test
    fun `Initial current project should be set`() {
        assertThat(
            currentProjectViewModel.currentProject.value,
            `is`(Project.Saved("123", "Project X", "X", "#cccccc"))
        )
    }

    @Test
    fun `setCurrentProject() sets current project`() {
        val project = Project.Saved("456", "Project Y", "Y", "#ffffff")

        currentProjectViewModel.setCurrentProject(project)
        verify(currentProjectProvider).setCurrentProject("456")
    }

    @Test
    fun `setCurrentProject() re-initializes analytics`() {
        val project = Project.Saved("456", "Project Y", "Y", "#ffffff")

        currentProjectViewModel.setCurrentProject(project)
        verify(analyticsInitializer).initialize()
    }
}
