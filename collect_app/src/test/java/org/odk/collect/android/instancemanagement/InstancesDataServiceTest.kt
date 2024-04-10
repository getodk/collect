package org.odk.collect.android.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.openrosa.HttpGetResult
import org.odk.collect.android.openrosa.OpenRosaHttpInterface
import org.odk.collect.android.projects.ProjectDependencyProviderFactory
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.forms.instances.Instance.STATUS_COMPLETE
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.projects.Project
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.testshared.BooleanChangeLock

@RunWith(AndroidJUnit4::class)
class InstancesDataServiceTest {
    val project = Project.Saved("1", "Test", "T", "#000000")

    private val formsRepositoryProvider = mock<FormsRepositoryProvider>().apply {
        whenever(get(project.uuid)).thenReturn(InMemFormsRepository())
    }

    private val instancesRepositoryProvider = mock<InstancesRepositoryProvider>().apply {
        whenever(get(project.uuid)).thenReturn(InMemInstancesRepository())
    }

    private val projectDataService = mock<ProjectsDataService>().apply {
        whenever(getCurrentProject()).thenReturn(project)
    }

    private val changeLockProvider = ChangeLockProvider { BooleanChangeLock() }

    val settingsProvider = InMemSettingsProvider().also {
        it.getUnprotectedSettings(project.uuid)
            .save(ProjectKeys.KEY_SERVER_URL, "http://example.com")
    }

    private val projectsDependencyProviderFactory = ProjectDependencyProviderFactory(
        settingsProvider,
        formsRepositoryProvider,
        instancesRepositoryProvider,
        mock(),
        changeLockProvider,
        mock(),
        mock(),
        mock()
    )

    private val projectDependencyProvider = projectsDependencyProviderFactory.create(project.uuid)

    private val httpInterface = mock<OpenRosaHttpInterface>()

    private val instancesDataService =
        InstancesDataService(
            mock(),
            mock(),
            projectDataService,
            projectsDependencyProviderFactory,
            mock(),
            mock(),
            httpInterface,
            mock()
        )

    @Test
    fun `instances should not be deleted if the instances database is locked`() {
        (projectDependencyProvider.instancesLock as BooleanChangeLock).lock()
        val result = instancesDataService.deleteInstances(longArrayOf(1))
        assertThat(result, equalTo(false))
    }

    @Test
    fun `instances should be deleted if the instances database is not locked`() {
        val result = instancesDataService.deleteInstances(longArrayOf(1))
        assertThat(result, equalTo(true))
    }

    @Test
    fun `autoSendInstances() returns true when there are no instances to send`() {
        val result = instancesDataService.autoSendInstances(project.uuid)
        assertThat(result, equalTo(true))
    }

    @Test
    fun `autoSendInstances() returns false when an instance fails to send`() {
        val formsRepository = projectDependencyProvider.formsRepository
        val form = formsRepository.save(FormFixtures.form())

        val instancesRepository = projectDependencyProvider.instancesRepository
        instancesRepository.save(InstanceFixtures.instance(form = form, status = STATUS_COMPLETE))

        whenever(httpInterface.executeGetRequest(any(), any(), any()))
            .doReturn(HttpGetResult(null, emptyMap(), "", 500))

        val result = instancesDataService.autoSendInstances(project.uuid)
        assertThat(result, equalTo(false))
    }
}
