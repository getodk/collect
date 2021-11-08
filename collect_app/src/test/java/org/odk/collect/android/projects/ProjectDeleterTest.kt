package org.odk.collect.android.projects

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.keys.ProtectedProjectKeys
import org.odk.collect.android.support.InMemSettingsProvider
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.shared.TempFiles
import org.odk.collect.testshared.BooleanChangeLock
import java.io.File

class ProjectDeleterTest {
    private val project1 = Project.Saved("1", "1", "1", "#ffffff")

    private val projectsRepository = InMemProjectsRepository()
    private val instancesRepository = InMemInstancesRepository()

    private val currentProjectProvider = mock<CurrentProjectProvider> {
        on { getCurrentProject() } doReturn project1
    }
    private val formUpdateManager = mock<FormUpdateScheduler>()
    private val instanceSubmitScheduler = mock<InstanceSubmitScheduler>()
    private val changeLockProvider = mock<ChangeLockProvider> {
        on { getFormLock(any()) } doReturn BooleanChangeLock()
        on { getInstanceLock(any()) } doReturn BooleanChangeLock()
    }
    private val settingsProvider = InMemSettingsProvider()

    @Before
    fun setup() {
        projectsRepository.save(project1)
    }

    @Test
    fun `If there are incomplete instances the project should not be deleted`() {
        instancesRepository.save(
            Instance.Builder()
                .status(Instance.STATUS_INCOMPLETE)
                .build()
        )

        val deleter = ProjectDeleter(
            projectsRepository,
            mock(),
            mock(),
            mock(),
            instancesRepository,
            "",
            changeLockProvider,
            settingsProvider
        )

        deleter.deleteCurrentProject()
        assertThat(deleter.deleteCurrentProject(), instanceOf(DeleteProjectResult.UnsentInstances::class.java))
        assertThat(projectsRepository.projects.contains(project1), `is`(true))
    }

    @Test
    fun `If there are complete instances the project should not be deleted`() {
        instancesRepository.save(
            Instance.Builder()
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        val deleter = ProjectDeleter(
            projectsRepository,
            mock(),
            mock(),
            mock(),
            instancesRepository,
            "",
            changeLockProvider,
            settingsProvider
        )

        deleter.deleteCurrentProject()
        assertThat(deleter.deleteCurrentProject(), instanceOf(DeleteProjectResult.UnsentInstances::class.java))
        assertThat(projectsRepository.projects.contains(project1), `is`(true))
    }

    @Test
    fun `If there are submission failed instances the project should not be deleted`() {
        instancesRepository.save(
            Instance.Builder()
                .status(Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )

        val deleter = ProjectDeleter(
            projectsRepository,
            mock(),
            mock(),
            mock(),
            instancesRepository,
            "",
            changeLockProvider,
            settingsProvider
        )

        deleter.deleteCurrentProject()
        assertThat(deleter.deleteCurrentProject(), instanceOf(DeleteProjectResult.UnsentInstances::class.java))
        assertThat(projectsRepository.projects.contains(project1), `is`(true))
    }

    @Test
    fun `If there are saved instances but all sent the project should be deleted`() {
        instancesRepository.save(
            Instance.Builder()
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )

        val deleter = ProjectDeleter(
            projectsRepository,
            currentProjectProvider,
            formUpdateManager,
            instanceSubmitScheduler,
            instancesRepository,
            "",
            changeLockProvider,
            settingsProvider
        )

        val result = deleter.deleteCurrentProject()
        assertThat(result, instanceOf(DeleteProjectResult.DeletedSuccessfully::class.java))
        assertThat((result as DeleteProjectResult.DeletedSuccessfully).newCurrentProject, `is`(nullValue()))
        assertThat(projectsRepository.projects.size, `is`(0))
    }

    @Test
    fun `If there are running background jobs that use blank forms the project should not be deleted`() {
        val formChangeLock = BooleanChangeLock()
        formChangeLock.lock()

        whenever(changeLockProvider.getFormLock(any())).thenReturn(formChangeLock)

        val deleter = ProjectDeleter(
            projectsRepository,
            currentProjectProvider,
            formUpdateManager,
            instanceSubmitScheduler,
            instancesRepository,
            "",
            changeLockProvider,
            settingsProvider
        )

        val result = deleter.deleteCurrentProject()
        assertThat(result, instanceOf(DeleteProjectResult.RunningBackgroundJobs::class.java))
        assertThat(projectsRepository.projects.contains(project1), `is`(true))
    }

    @Test
    fun `If there are running background jobs that use saved forms the project should not be deleted`() {
        val changeLock = BooleanChangeLock()
        changeLock.lock()

        whenever(changeLockProvider.getInstanceLock(any())).thenReturn(changeLock)

        val deleter = ProjectDeleter(
            projectsRepository,
            currentProjectProvider,
            formUpdateManager,
            instanceSubmitScheduler,
            instancesRepository,
            "",
            changeLockProvider,
            settingsProvider
        )

        val result = deleter.deleteCurrentProject()
        assertThat(result, instanceOf(DeleteProjectResult.RunningBackgroundJobs::class.java))
        assertThat(projectsRepository.projects.contains(project1), `is`(true))
    }

    @Test
    fun `Deleting project cancels scheduled form updates and instance submits`() {
        val deleter = ProjectDeleter(
            projectsRepository,
            currentProjectProvider,
            formUpdateManager,
            instanceSubmitScheduler,
            instancesRepository,
            "",
            changeLockProvider,
            settingsProvider
        )

        deleter.deleteCurrentProject()
        verify(formUpdateManager).cancelUpdates(project1.uuid)
        verify(instanceSubmitScheduler).cancelSubmit(project1.uuid)
    }

    @Test
    fun `Deleting project clears its settings`() {
        settingsProvider.getMetaSettings().save(MetaKeys.KEY_INSTALL_ID, "1234")

        settingsProvider.getUnprotectedSettings("1").save(ProjectKeys.KEY_SERVER_URL, "https://my-server.com")
        settingsProvider.getProtectedSettings("1").save(ProtectedProjectKeys.KEY_AUTOSEND, false)

        settingsProvider.getUnprotectedSettings("2").save(ProjectKeys.KEY_SERVER_URL, "https://my-server.com")
        settingsProvider.getProtectedSettings("2").save(ProtectedProjectKeys.KEY_AUTOSEND, false)

        val deleter = ProjectDeleter(
            mock(),
            currentProjectProvider,
            mock(),
            mock(),
            mock(),
            "",
            changeLockProvider,
            settingsProvider
        )

        deleter.deleteCurrentProject()

        assertThat(settingsProvider.getMetaSettings().getString(MetaKeys.KEY_INSTALL_ID), `is`("1234"))

        settingsProvider.getUnprotectedSettings("1").getAll().forEach { (key, value) ->
            assertThat(value, `is`(ProtectedProjectKeys.defaults[key]))
        }

        settingsProvider.getProtectedSettings("1").getAll().forEach { (key, value) ->
            assertThat(value, `is`(ProtectedProjectKeys.defaults[key]))
        }

        assertThat(settingsProvider.getUnprotectedSettings("2").getString(ProjectKeys.KEY_SERVER_URL), `is`("https://my-server.com"))
        assertThat(settingsProvider.getProtectedSettings("2").getBoolean(ProtectedProjectKeys.KEY_AUTOSEND), `is`(false))
    }

    @Test
    fun `If the deleted project was the last one return DeletedSuccessfully with null parameter`() {
        val deleter = ProjectDeleter(
            projectsRepository,
            currentProjectProvider,
            mock(),
            mock(),
            instancesRepository,
            "",
            changeLockProvider,
            settingsProvider
        )

        val result = deleter.deleteCurrentProject()
        assertThat(result, instanceOf(DeleteProjectResult.DeletedSuccessfully::class.java))
        assertThat((result as DeleteProjectResult.DeletedSuccessfully).newCurrentProject, `is`(nullValue()))
    }

    @Test
    fun `If the deleted project was not the last one set the current project and return the new current one`() {
        val project2 = Project.Saved("2", "2", "2", "#cccccc")
        projectsRepository.save(project2)

        val deleter = ProjectDeleter(
            projectsRepository,
            currentProjectProvider,
            mock(),
            mock(),
            instancesRepository,
            "",
            changeLockProvider,
            settingsProvider
        )

        val result = deleter.deleteCurrentProject()
        verify(currentProjectProvider).setCurrentProject(project2.uuid)
        assertThat(result, instanceOf(DeleteProjectResult.DeletedSuccessfully::class.java))
        assertThat((result as DeleteProjectResult.DeletedSuccessfully).newCurrentProject, `is`(project2))
    }

    @Test
    fun `Project directory should be removed`() {
        val projectDir = TempFiles.createTempDir()
        File(projectDir, "dir").mkdir()

        assertThat(projectDir.exists(), `is`(true))
        assertThat(projectDir.listFiles().size, `is`(1))

        val deleter = ProjectDeleter(
            projectsRepository,
            currentProjectProvider,
            mock(),
            mock(),
            instancesRepository,
            projectDir.absolutePath,
            changeLockProvider,
            settingsProvider
        )

        deleter.deleteCurrentProject()
        assertThat(projectDir.exists(), `is`(false))
    }
}
