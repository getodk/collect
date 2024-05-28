package org.odk.collect.android.instancemanagement

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.openrosa.HttpGetResult
import org.odk.collect.android.openrosa.OpenRosaHttpInterface
import org.odk.collect.android.projects.ProjectDependencyFactory
import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.android.utilities.ChangeLocks
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.forms.instances.Instance.STATUS_COMPLETE
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.settings.enums.AutoSend
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.InMemSettings
import org.odk.collect.testshared.BooleanChangeLock

@RunWith(AndroidJUnit4::class)
class InstancesDataServiceTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()

    private val settings = InMemSettings().also {
        it.save(ProjectKeys.KEY_SERVER_URL, "http://example.com")
        it.save(ProjectKeys.KEY_AUTOSEND, AutoSend.WIFI_ONLY.getValue(application))
    }

    private val changeLocks = ChangeLocks(BooleanChangeLock(), BooleanChangeLock())
    private val formsRepository = InMemFormsRepository()
    private val instancesRepository = InMemInstancesRepository()

    private val projectsDependencyModuleFactory = ProjectDependencyFactory.from {
        ProjectDependencyModule(
            it,
            ProjectDependencyFactory.from { settings },
            ProjectDependencyFactory.from { formsRepository },
            ProjectDependencyFactory.from { instancesRepository },
            mock(),
            ProjectDependencyFactory.from { changeLocks },
            mock(),
            mock(),
            mock()
        )
    }

    private val projectDependencyProvider = projectsDependencyModuleFactory.create("blah")
    private val httpInterface = mock<OpenRosaHttpInterface>()
    private val notifier = mock<Notifier>()

    private val instancesDataService =
        InstancesDataService(
            AppState(),
            mock(),
            projectsDependencyModuleFactory,
            notifier,
            mock(),
            httpInterface,
            mock()
        )

    @Test
    fun `instances should not be deleted if the instances database is locked`() {
        (projectDependencyProvider.instancesLock as BooleanChangeLock).lock()
        val result = instancesDataService.deleteInstances("projectId", longArrayOf(1))
        assertThat(result, equalTo(false))
    }

    @Test
    fun `instances should be deleted if the instances database is not locked`() {
        val result = instancesDataService.deleteInstances("projectId", longArrayOf(1))
        assertThat(result, equalTo(true))
    }

    @Test
    fun `sendInstances() returns true when there are no instances to send`() {
        val result = instancesDataService.sendInstances("projectId")
        assertThat(result, equalTo(true))
    }

    @Test
    fun `sendInstances() does not notify when there are no instances to send`() {
        instancesDataService.sendInstances("projectId")
        verifyNoInteractions(notifier)
    }

    @Test
    fun `sendInstances() returns false when an instance fails to send`() {
        val formsRepository = projectDependencyProvider.formsRepository
        val form = formsRepository.save(FormFixtures.form())

        val instancesRepository = projectDependencyProvider.instancesRepository
        instancesRepository.save(InstanceFixtures.instance(form = form, status = STATUS_COMPLETE))

        whenever(httpInterface.executeGetRequest(any(), any(), any()))
            .doReturn(HttpGetResult(null, emptyMap(), "", 500))

        val result = instancesDataService.sendInstances("projectId")
        assertThat(result, equalTo(false))
    }
}
