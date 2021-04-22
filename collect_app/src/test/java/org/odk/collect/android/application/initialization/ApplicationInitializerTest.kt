package org.odk.collect.android.application.initialization

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.ProjectImporter
import org.odk.collect.android.support.RobolectricHelpers
import org.odk.collect.android.utilities.AppStateProvider
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.UUIDGenerator
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ApplicationInitializerTest {
    @Mock
    lateinit var appStateProvider: AppStateProvider

    @Mock
    lateinit var projectsRepository: ProjectsRepository

    @Mock
    lateinit var projectImporter: ProjectImporter

    lateinit var applicationInitializer: ApplicationInitializer

    @Before
    fun setup() {
        appStateProvider = mock(AppStateProvider::class.java)
        projectsRepository = mock(ProjectsRepository::class.java)
        projectImporter = mock(ProjectImporter::class.java)

        RobolectricHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesAppStateProvider(context: Context): AppStateProvider {
                return appStateProvider
            }

            override fun providesProjectsRepository(uuidGenerator: UUIDGenerator, gson: Gson, settingsProvider: SettingsProvider): ProjectsRepository {
                return projectsRepository
            }

            override fun providesProjectImporter(projectsRepository: ProjectsRepository, settingsProvider: SettingsProvider): ProjectImporter {
                return projectImporter
            }
        })

        applicationInitializer = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Collect>()).applicationInitializer()
    }

    @Test
    fun `Should existing project be imported when it's not first launch and projects repository is empty`() {
        `when`(appStateProvider.isFreshInstall()).thenReturn(false)
        `when`(projectsRepository.getAll()).thenReturn(emptyList())
        applicationInitializer.initialize()
        verify(projectImporter).importExistingProject()
    }

    @Test
    fun `Should not existing project be imported when it's first launch`() {
        `when`(appStateProvider.isFreshInstall()).thenReturn(true)
        `when`(projectsRepository.getAll()).thenReturn(emptyList())
        applicationInitializer.initialize()
        verifyNoInteractions(projectImporter)
    }
}
