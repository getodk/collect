package org.odk.collect.android.projects

import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.backgroundwork.FormUpdateManager
import org.odk.collect.projects.Project

class ProjectDeleterTest {

    @Test
    fun deletingProject_cancelsScheduledFormUpdates() {
        val currentProjectProvider = mock<CurrentProjectProvider> {
            on { getCurrentProject() } doReturn Project.Saved("id", "name", "i", "#ffffff")
        }

        val formUpdateManager = mock<FormUpdateManager>()
        val deleter = ProjectDeleter(mock(), currentProjectProvider, formUpdateManager)

        deleter.deleteCurrentProject()
        verify(formUpdateManager).cancelUpdates()
    }
}
