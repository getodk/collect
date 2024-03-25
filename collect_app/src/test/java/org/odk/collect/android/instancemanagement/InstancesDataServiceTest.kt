package org.odk.collect.android.instancemanagement

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.projects.Project
import org.odk.collect.testshared.BooleanChangeLock

class InstancesDataServiceTest {
    private val formsRepositoryProvider = mock<FormsRepositoryProvider>().apply {
        whenever(get()).thenReturn(mock())
    }
    private val instancesRepositoryProvider = mock<InstancesRepositoryProvider>().apply {
        whenever(get()).thenReturn(mock())
    }
    val project = Project.Saved("1", "Test", "T", "#000000")
    private val projectDataService = mock<ProjectsDataService>().apply {
        whenever(getCurrentProject()).thenReturn(project)
    }
    private val changeLock = BooleanChangeLock()
    private val changeLockProvider = mock<ChangeLockProvider>().apply {
        whenever(getInstanceLock(project.uuid)).thenReturn(changeLock)
    }
    private val instancesDataService =
        InstancesDataService(mock(), formsRepositoryProvider, instancesRepositoryProvider, mock(), mock(), mock(), mock(), projectDataService, changeLockProvider, mock())

    @Test
    fun `instances should not be deleted if the instances database is locked`() {
        changeLock.lock()
        val result = instancesDataService.deleteInstances(longArrayOf(1))
        assertThat(result, equalTo(false))
    }

    @Test
    fun `instances should be deleted if the instances database is not locked`() {
        val result = instancesDataService.deleteInstances(longArrayOf(1))
        assertThat(result, equalTo(true))
    }
}
