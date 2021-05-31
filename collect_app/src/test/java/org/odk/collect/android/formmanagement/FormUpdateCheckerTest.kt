package org.odk.collect.android.formmanagement

import android.app.Application
import android.database.ContentObserver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.provider.FormsProviderAPI.CONTENT_URI
import org.odk.collect.android.support.BooleanChangeLock
import org.odk.collect.forms.FormSource
import org.odk.collect.projects.Project

@RunWith(AndroidJUnit4::class)
class FormUpdateCheckerTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val component = DaggerUtils.getComponent(application)

    private val formSource = mock<FormSource> {
        on { fetchFormList() } doReturn emptyList()
    }

    private lateinit var updateChecker: FormUpdateChecker

    @Before
    fun setup() {
        val settingsProvider = component.settingsProvider()
        val storagePathProvider = component.storagePathProvider()
        val formsRepositoryProvider = component.formsRepositoryProvider()

        val formSourceProvider = mock<FormSourceProvider> { on { get(any()) } doReturn formSource }

        updateChecker = FormUpdateChecker(
            context = application,
            notifier = mock(),
            analytics = mock(),
            changeLock = BooleanChangeLock(),
            storagePathProvider = storagePathProvider,
            settingsProvider = settingsProvider,
            formsRepositoryProvider = formsRepositoryProvider,
            formSourceProvider = formSourceProvider
        )
    }

    @Test
    fun `checkForUpdates() notifies Forms content resolver`() {
        val contentObserver = mock<ContentObserver>()
        application.contentResolver.registerContentObserver(CONTENT_URI, false, contentObserver)

        val project = setupProject()
        updateChecker.checkForUpdates(project.uuid)

        verify(contentObserver).dispatchChange(false, CONTENT_URI)
    }

    @Test
    fun `checkForUpdates() uses projectId`() {
        val project = setupProject()
        assertThat(updateChecker.checkForUpdates(project.uuid), `is`(true))
    }

    private fun setupProject(): Project.Saved {
        val projectImporter = component.projectImporter()
        val projectsRepository = component.projectsRepository()
        val project = projectsRepository.save(Project.New("blah", "B", "#ffffff"))
        projectImporter.setupProject(project)
        return project
    }
}
